package com.smart.investment.module.news.es;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smart.investment.common.core.es.BaseSearchService;
import com.smart.investment.common.core.es.IndexManager;
import com.smart.investment.common.core.result.PageResult;
import com.smart.investment.module.news.entity.NewsArticle;
import com.smart.investment.module.news.mapper.NewsArticleMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 新闻 ES 搜索服务
 * <p>
 * 继承 BaseSearchService，实现新闻全文搜索。
 * ES 不可用时降级为数据库 LIKE 查询。
 */
@Slf4j
@Service
public class NewsSearchService extends BaseSearchService<NewsArticleDocument> {

    private final NewsArticleMapper newsArticleMapper;

    public NewsSearchService(ElasticsearchClient esClient, NewsArticleMapper newsArticleMapper) {
        super(esClient);
        this.newsArticleMapper = newsArticleMapper;
    }

    @Override
    protected String getIndexName() {
        return IndexManager.INDEX_NEWS_ARTICLE;
    }

    @Override
    protected Class<NewsArticleDocument> getDocumentClass() {
        return NewsArticleDocument.class;
    }

    @Override
    protected List<String> getHighlightFields() {
        return List.of("content", "title");
    }

    /**
     * 将高亮结果注入文档
     */
    @Override
    protected void applyHighlight(NewsArticleDocument document, Map<String, List<String>> highlights) {
        if (highlights == null || highlights.isEmpty()) {
            return;
        }
        List<String> contentHighlights = highlights.getOrDefault("content", Collections.emptyList());
        if (!contentHighlights.isEmpty()) {
            document.setContent(String.join(" ... ", contentHighlights));
        }
        List<String> titleHighlights = highlights.getOrDefault("title", Collections.emptyList());
        if (!titleHighlights.isEmpty()) {
            document.setTitle(titleHighlights.get(0));
        }
    }

    /**
     * ES 不可用时降级为数据库 LIKE 查询
     */
    @Override
    protected PageResult<NewsArticleDocument> fallbackSearch(String keyword, int page, int size) {
        log.info("ES 不可用，降级为 MySQL LIKE 查询: keyword={}", keyword);
        try {
            Page<NewsArticle> pageParam = new Page<>(page, size);
            LambdaQueryWrapper<NewsArticle> wrapper = new LambdaQueryWrapper<NewsArticle>()
                    .and(w -> w.like(NewsArticle::getTitle, keyword)
                            .or().like(NewsArticle::getContent, keyword))
                    .orderByDesc(NewsArticle::getPublishedAt);

            Page<NewsArticle> result = newsArticleMapper.selectPage(pageParam, wrapper);

            List<NewsArticleDocument> docs = result.getRecords().stream()
                    .map(this::toDocument)
                    .collect(Collectors.toList());

            return PageResult.of(docs, result.getTotal(), (long) page, (long) size);
        } catch (Exception e) {
            log.error("MySQL 降级查询失败: {}", e.getMessage());
            return PageResult.empty((long) page, (long) size);
        }
    }

    /**
     * 将 NewsArticle 转换为 ES 文档（用于降级查询）
     */
    private NewsArticleDocument toDocument(NewsArticle article) {
        List<String> relatedSecurities = new ArrayList<>();
        if (article.getRelatedSecurities() != null && !article.getRelatedSecurities().isEmpty()) {
            // 保持 JSON 字符串作为列表中的一员
            relatedSecurities.add(article.getRelatedSecurities());
        }

        return NewsArticleDocument.builder()
                .id(article.getId())
                .source(article.getSource())
                .sourceUrl(article.getUrl())
                .title(article.getTitle())
                .content(article.getContent())
                .sentiment(article.getSentiment())
                .sentimentScore(article.getSentimentScore() != null ? article.getSentimentScore().doubleValue() : null)
                .relatedSecurities(relatedSecurities)
                .publishedAt(article.getPublishedAt())
                .crawledAt(article.getCrawledAt())
                .build();
    }
}
