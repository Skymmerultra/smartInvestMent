package com.smart.investment.module.news.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smart.investment.common.core.config.MqDeclareConfig;
import com.smart.investment.common.core.constant.CacheKeys;
import com.smart.investment.common.core.constant.Constants;
import com.smart.investment.common.core.exception.BusinessException;
import com.smart.investment.common.core.exception.ErrorCode;
import com.smart.investment.common.core.mq.BaseMessage;
import com.smart.investment.common.core.result.PageResult;
import com.smart.investment.common.core.utils.JsonUtils;
import com.smart.investment.common.core.websocket.WebSocketPushService;
import com.smart.investment.module.news.ai.NewsAiService;
import com.smart.investment.module.news.dto.*;
import com.smart.investment.module.news.entity.NewsArticle;
import com.smart.investment.module.news.entity.NewsSentiment;
import com.smart.investment.module.news.es.NewsArticleDocument;
import com.smart.investment.module.news.es.NewsSearchService;
import com.smart.investment.module.news.mapper.NewsArticleMapper;
import com.smart.investment.module.news.mapper.NewsSentimentMapper;
import com.smart.investment.module.news.util.SimHashUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 新闻服务
 * <p>
 * 负责新闻 CRUD、情感分析汇总、全文搜索、SimHash 去重、数据清理。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NewsService {

    private final NewsArticleMapper newsArticleMapper;
    private final NewsSentimentMapper newsSentimentMapper;
    private final NewsAiService newsAiService;
    private final NewsSearchService newsSearchService;
    private final RabbitTemplate rabbitTemplate;
    private final WebSocketPushService webSocketPushService;

    // ==================== 新闻查询 ====================

    /**
     * 最新新闻列表（分页，Redis 缓存 300s）
     */
    @Cacheable(value = "newsList", key = "'page_' + #page + '_' + #size")
    public PageResult<NewsDetailResponse> listLatestNews(int page, int size) {
        IPage<NewsArticle> iPage = newsArticleMapper.selectPage(
                new Page<>(page, size),
                new LambdaQueryWrapper<NewsArticle>()
                        .orderByDesc(NewsArticle::getPublishedAt));

        List<NewsDetailResponse> records = iPage.getRecords().stream()
                .map(this::toDetailResponse)
                .collect(Collectors.toList());

        return PageResult.of(records, iPage.getTotal(), iPage.getCurrent(), iPage.getSize());
    }

    /**
     * 新闻详情
     */
    public NewsDetailResponse getNewsById(Long id) {
        NewsArticle article = newsArticleMapper.selectById(id);
        if (article == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "新闻不存在");
        }
        return toDetailResponse(article);
    }

    /**
     * 新闻情感分析汇总（按来源/时间聚合统计 POSITIVE/NEGATIVE/NEUTRAL 占比）
     */
    public NewsSentimentSummaryResponse getSentimentSummary() {
        // 统计总数
        Long totalCount = newsArticleMapper.selectCount(new LambdaQueryWrapper<>());

        // 按来源聚合
        List<NewsArticle> allArticles = newsArticleMapper.selectList(
                new LambdaQueryWrapper<NewsArticle>().isNotNull(NewsArticle::getSentiment));
        List<NewsSentimentSummaryResponse.SourceSummary> bySource = aggregateBySource(allArticles);

        // 按日期聚合（最近7天）
        List<NewsSentimentSummaryResponse.DateSummary> byDate = aggregateByDate(allArticles);

        return NewsSentimentSummaryResponse.builder()
                .totalCount(totalCount != null ? totalCount : 0)
                .bySource(bySource)
                .byDate(byDate)
                .build();
    }

    /**
     * ES 全文搜索新闻
     */
    public PageResult<NewsArticleDocument> searchNews(String keyword, int page, int size) {
        return newsSearchService.searchPage(keyword, page, size);
    }

    // ==================== 爬虫消息处理 ====================

    /**
     * 处理爬虫消息：SimHash 去重 → 持久化 → ES 索引 → 发送情感分析消息
     */
    @Transactional(rollbackFor = Exception.class)
    public void processCrawlMessage(NewsCrawlMessage crawlMessage) {
        // 1. SimHash 去重校验（与最近24小时新闻比对）
        String textToHash = crawlMessage.getTitle() + crawlMessage.getContent();
        long hash = SimHashUtils.simHash(textToHash);

        LocalDateTime since24h = LocalDateTime.now().minusHours(24);
        List<NewsArticle> recentArticles = newsArticleMapper.selectList(
                new LambdaQueryWrapper<NewsArticle>()
                        .ge(NewsArticle::getCreatedAt, since24h));

        for (NewsArticle existing : recentArticles) {
            String existingText = existing.getTitle() + existing.getContent();
            long existingHash = SimHashUtils.simHash(existingText);
            if (SimHashUtils.isDuplicate(hash, existingHash)) {
                log.info("SimHash 去重命中，丢弃重复新闻: title={}, source={}",
                        crawlMessage.getTitle(), crawlMessage.getSource());
                return;
            }
        }

        // 2. 持久化到 news_article 表
        LocalDateTime now = LocalDateTime.now();
        NewsArticle article = NewsArticle.builder()
                .source(crawlMessage.getSource())
                .title(crawlMessage.getTitle())
                .content(crawlMessage.getContent())
                .url(crawlMessage.getUrl())
                .publishedAt(crawlMessage.getPublishedAt() != null
                        ? crawlMessage.getPublishedAt() : now)
                .crawledAt(now)
                .createdAt(now)
                .build();
        newsArticleMapper.insert(article);

        final Long newsId = article.getId();
        log.info("新闻持久化成功: id={}, title={}", newsId, article.getTitle());

        // 3. 索引至 ES（事务提交后再执行）
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                indexToEs(article);
            }
        });

        // 4. 发送情感分析消息到 q.news.sentiment
        final NewsSentimentMessage sentimentMessage = NewsSentimentMessage.builder()
                .newsId(newsId)
                .build();
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                BaseMessage<NewsSentimentMessage> message =
                        BaseMessage.of("NEWS_SENTIMENT", sentimentMessage);
                rabbitTemplate.convertAndSend(
                        MqDeclareConfig.EXCHANGE_NEWS,
                        MqDeclareConfig.RK_NEWS_SENTIMENT,
                        message);
                log.info("情感分析消息已发送: newsId={}", newsId);
            }
        });

        // 5. WebSocket 推送实时新闻
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                NewsDetailResponse response = toDetailResponse(article);
                webSocketPushService.pushNewsFeed(response);
                log.debug("WebSocket 新闻推送完成: newsId={}", newsId);
            }
        });
    }

    // ==================== 情感分析处理 ====================

    /**
     * 处理情感分析：调用 AI → 更新 news_article → 写入 news_sentiment → 发送趋势消息
     */
    @Transactional(rollbackFor = Exception.class)
    public void processSentiment(Long newsId) {
        log.info("开始情感分析: newsId={}", newsId);

        NewsArticle article = newsArticleMapper.selectById(newsId);
        if (article == null) {
            log.error("新闻不存在，无法进行情感分析: newsId={}", newsId);
            return;
        }

        // 1. 调用 AI 进行情感分析
        SentimentResult result = newsAiService.analyzeSentiment(
                article.getTitle(), article.getContent());
        if (result == null) {
            log.error("情感分析返回空结果: newsId={}", newsId);
            return;
        }

        // 2. 更新 news_article 情感字段
        String sentimentLabel = result.getSentiment() != null
                ? result.getSentiment().toUpperCase() : "NEUTRAL";
        BigDecimal score = BigDecimal.valueOf(
                result.getSentimentScore() != null ? result.getSentimentScore() : 0.5);
        score = score.setScale(4, RoundingMode.HALF_UP);

        String relatedSecuritiesJson = null;
        if (result.getRelatedSecurities() != null && !result.getRelatedSecurities().isEmpty()) {
            relatedSecuritiesJson = JsonUtils.toJsonString(result.getRelatedSecurities());
        }

        article.setSentiment(sentimentLabel);
        article.setSentimentScore(score);
        article.setRelatedSecurities(relatedSecuritiesJson);
        newsArticleMapper.updateById(article);

        // 3. 写入 news_sentiment 表
        LocalDateTime now = LocalDateTime.now();
        if (result.getRelatedSecurities() != null) {
            for (SentimentResult.RelatedSecurity security : result.getRelatedSecurities()) {
                NewsSentiment sentiment = NewsSentiment.builder()
                        .newsId(newsId)
                        .sentimentLabel(sentimentLabel)
                        .sentimentScore(score)
                        .relatedSecurity(security.getCode())
                        .aiModel("DeepSeek")
                        .analyzedAt(now)
                        .build();
                newsSentimentMapper.insert(sentiment);
            }
        }

        // 如果没有相关证券，也记录一条汇总记录
        if (result.getRelatedSecurities() == null || result.getRelatedSecurities().isEmpty()) {
            NewsSentiment sentiment = NewsSentiment.builder()
                    .newsId(newsId)
                    .sentimentLabel(sentimentLabel)
                    .sentimentScore(score)
                    .aiModel("DeepSeek")
                    .analyzedAt(now)
                    .build();
            newsSentimentMapper.insert(sentiment);
        }

        // 4. 发送情感结果到 q.news.trend 供趋势预测模块消费
        Map<String, Object> trendPayload = new LinkedHashMap<>();
        trendPayload.put("newsId", newsId);
        trendPayload.put("sentiment", sentimentLabel);
        trendPayload.put("sentimentScore", score);
        trendPayload.put("relatedSecurities", result.getRelatedSecurities());
        trendPayload.put("impactSummary", result.getImpactSummary());
        trendPayload.put("analyzedAt", now.toString());

        BaseMessage<Map<String, Object>> trendMessage =
                BaseMessage.of("NEWS_TREND", trendPayload);
        rabbitTemplate.convertAndSend(
                MqDeclareConfig.EXCHANGE_NEWS,
                MqDeclareConfig.RK_NEWS_TREND,
                trendMessage);
        log.info("趋势预测消息已发送: newsId={}, sentiment={}", newsId, sentimentLabel);
    }

    // ==================== 数据清理 ====================

    /**
     * 每日凌晨清理30天前的新闻数据
     */
    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "newsList", allEntries = true)
    public void cleanupOldNews() {
        LocalDateTime threshold = LocalDateTime.now().minusDays(Constants.NEWS_RETENTION_DAYS);

        LambdaQueryWrapper<NewsArticle> wrapper = new LambdaQueryWrapper<NewsArticle>()
                .lt(NewsArticle::getCreatedAt, threshold);

        long count = newsArticleMapper.selectCount(wrapper);
        if (count > 0) {
            newsArticleMapper.delete(wrapper);
            log.info("新闻数据清理完成: 删除 {} 条超过 {} 天的新闻记录",
                    count, Constants.NEWS_RETENTION_DAYS);
        } else {
            log.debug("没有需要清理的新闻数据");
        }
    }

    // ==================== ES 索引 ====================

    /**
     * 将新闻索引至 ES
     */
    public void indexToEs(NewsArticle article) {
        try {
            List<String> relatedSecurities = new ArrayList<>();
            if (article.getRelatedSecurities() != null && !article.getRelatedSecurities().isEmpty()) {
                try {
                    List<Map<String, Object>> securities =
                            JsonUtils.parseObject(article.getRelatedSecurities(),
                                    new com.fasterxml.jackson.core.type.TypeReference<List<Map<String, Object>>>() {});
                    relatedSecurities = securities.stream()
                            .map(s -> (String) s.getOrDefault("code", ""))
                            .filter(c -> !c.isEmpty())
                            .collect(Collectors.toList());
                } catch (Exception e) {
                    log.warn("解析 relatedSecurities JSON 失败: {}", e.getMessage());
                }
            }

            NewsArticleDocument doc = NewsArticleDocument.builder()
                    .id(article.getId())
                    .source(article.getSource())
                    .sourceUrl(article.getUrl())
                    .title(article.getTitle())
                    .content(article.getContent())
                    .sentiment(article.getSentiment())
                    .sentimentScore(article.getSentimentScore() != null
                            ? article.getSentimentScore().doubleValue() : null)
                    .relatedSecurities(relatedSecurities)
                    .publishedAt(article.getPublishedAt())
                    .crawledAt(article.getCrawledAt())
                    .build();
            newsSearchService.index(String.valueOf(article.getId()), doc);
            log.debug("ES 索引完成: newsId={}", article.getId());
        } catch (Exception e) {
            log.error("ES 索引失败: newsId={}, error={}", article.getId(), e.getMessage());
        }
    }

    // ==================== 私有方法 ====================

    /**
     * 转换为详情响应
     */
    private NewsDetailResponse toDetailResponse(NewsArticle article) {
        List<NewsDetailResponse.RelatedSecurity> securities = new ArrayList<>();
        if (article.getRelatedSecurities() != null && !article.getRelatedSecurities().isEmpty()) {
            try {
                securities = JsonUtils.parseObject(article.getRelatedSecurities(),
                        new com.fasterxml.jackson.core.type.TypeReference<List<NewsDetailResponse.RelatedSecurity>>() {});
            } catch (Exception e) {
                log.warn("解析 relatedSecurities 失败: {}", e.getMessage());
            }
        }

        return NewsDetailResponse.builder()
                .id(article.getId())
                .source(article.getSource())
                .title(article.getTitle())
                .content(article.getContent())
                .url(article.getUrl())
                .sentiment(article.getSentiment())
                .sentimentScore(article.getSentimentScore())
                .relatedSecurities(securities)
                .publishedAt(article.getPublishedAt())
                .crawledAt(article.getCrawledAt())
                .createdAt(article.getCreatedAt())
                .build();
    }

    /**
     * 按来源聚合情感统计
     */
    private List<NewsSentimentSummaryResponse.SourceSummary> aggregateBySource(List<NewsArticle> articles) {
        Map<String, Map<String, Long>> sourceStats = new LinkedHashMap<>();

        for (NewsArticle article : articles) {
            String source = article.getSource() != null ? article.getSource() : "未知来源";
            String sentiment = article.getSentiment() != null ? article.getSentiment() : "NEUTRAL";

            sourceStats.computeIfAbsent(source, k -> {
                Map<String, Long> m = new LinkedHashMap<>();
                m.put("POSITIVE", 0L);
                m.put("NEGATIVE", 0L);
                m.put("NEUTRAL", 0L);
                return m;
            });
            sourceStats.get(source).merge(sentiment, 1L, Long::sum);
        }

        return sourceStats.entrySet().stream()
                .map(entry -> {
                    Map<String, Long> stats = entry.getValue();
                    long total = stats.values().stream().mapToLong(Long::longValue).sum();
                    return NewsSentimentSummaryResponse.SourceSummary.builder()
                            .source(entry.getKey())
                            .positiveCount(stats.getOrDefault("POSITIVE", 0L))
                            .negativeCount(stats.getOrDefault("NEGATIVE", 0L))
                            .neutralCount(stats.getOrDefault("NEUTRAL", 0L))
                            .totalCount(total)
                            .build();
                })
                .collect(Collectors.toList());
    }

    /**
     * 按日期聚合情感统计
     */
    private List<NewsSentimentSummaryResponse.DateSummary> aggregateByDate(List<NewsArticle> articles) {
        Map<String, Map<String, Long>> dateStats = new LinkedHashMap<>();

        for (NewsArticle article : articles) {
            String date;
            if (article.getPublishedAt() != null) {
                date = article.getPublishedAt().toLocalDate().toString();
            } else if (article.getCreatedAt() != null) {
                date = article.getCreatedAt().toLocalDate().toString();
            } else {
                date = LocalDate.now().toString();
            }

            String sentiment = article.getSentiment() != null ? article.getSentiment() : "NEUTRAL";

            dateStats.computeIfAbsent(date, k -> {
                Map<String, Long> m = new LinkedHashMap<>();
                m.put("POSITIVE", 0L);
                m.put("NEGATIVE", 0L);
                m.put("NEUTRAL", 0L);
                return m;
            });
            dateStats.get(date).merge(sentiment, 1L, Long::sum);
        }

        return dateStats.entrySet().stream()
                .sorted(Map.Entry.<String, Map<String, Long>>comparingByKey().reversed())
                .map(entry -> {
                    Map<String, Long> stats = entry.getValue();
                    long total = stats.values().stream().mapToLong(Long::longValue).sum();
                    return NewsSentimentSummaryResponse.DateSummary.builder()
                            .date(entry.getKey())
                            .positiveCount(stats.getOrDefault("POSITIVE", 0L))
                            .negativeCount(stats.getOrDefault("NEGATIVE", 0L))
                            .neutralCount(stats.getOrDefault("NEUTRAL", 0L))
                            .totalCount(total)
                            .build();
                })
                .collect(Collectors.toList());
    }
}
