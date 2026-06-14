package com.smart.investment.module.news.controller;

import com.smart.investment.common.core.constant.Constants;
import com.smart.investment.common.core.result.PageResult;
import com.smart.investment.common.core.result.Result;
import com.smart.investment.module.news.dto.NewsDetailResponse;
import com.smart.investment.module.news.dto.NewsSentimentSummaryResponse;
import com.smart.investment.module.news.es.NewsArticleDocument;
import com.smart.investment.module.news.service.NewsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 新闻资讯控制器 (T-17)
 * <p>
 * 提供新闻列表查询、详情、情感分析汇总和全文搜索接口。
 */
@Slf4j
@RestController
@RequestMapping("/api/news")
@RequiredArgsConstructor
@Tag(name = "新闻资讯", description = "新闻列表查询、情感分析、全文搜索")
public class NewsController {

    private final NewsService newsService;

    /**
     * 最新新闻列表（分页，Redis 缓存 300s）
     */
    @GetMapping("/latest")
    @Operation(summary = "最新新闻列表", description = "分页查询最新新闻列表，Redis 缓存 300 秒")
    public Result<PageResult<NewsDetailResponse>> latest(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "20") int size) {

        if (page < 1) page = Constants.DEFAULT_PAGE;
        if (size < 1 || size > Constants.MAX_PAGE_SIZE) size = Constants.DEFAULT_PAGE_SIZE;

        PageResult<NewsDetailResponse> result = newsService.listLatestNews(page, size);
        return Result.success(result);
    }

    /**
     * 新闻详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "新闻详情", description = "根据新闻ID查询新闻详情")
    public Result<NewsDetailResponse> getById(
            @Parameter(description = "新闻ID") @PathVariable("id") Long id) {
        NewsDetailResponse response = newsService.getNewsById(id);
        return Result.success(response);
    }

    /**
     * 新闻情感分析汇总（按来源/时间聚合统计）
     */
    @GetMapping("/sentiment")
    @Operation(summary = "新闻情感分析汇总", description = "按来源和时间聚合统计 POSITIVE/NEGATIVE/NEUTRAL 占比")
    public Result<NewsSentimentSummaryResponse> sentiment() {
        NewsSentimentSummaryResponse response = newsService.getSentimentSummary();
        return Result.success(response);
    }

    /**
     * 新闻全文搜索（走 ES idx_news_article）
     */
    @GetMapping("/search")
    @Operation(summary = "新闻全文搜索", description = "通过 Elasticsearch 全文搜索新闻内容，返回高亮结果")
    public Result<PageResult<NewsArticleDocument>> search(
            @Parameter(description = "搜索关键词") @RequestParam String keyword,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "20") int size) {

        if (page < 1) page = Constants.DEFAULT_PAGE;
        if (size < 1 || size > Constants.MAX_PAGE_SIZE) size = Constants.DEFAULT_PAGE_SIZE;

        PageResult<NewsArticleDocument> result = newsService.searchNews(keyword, page, size);
        return Result.success(result);
    }
}
