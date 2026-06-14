package com.smart.investment.module.news.es;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * ES 新闻文档（对应 idx_news_article 索引）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class NewsArticleDocument {

    private Long id;

    private String source;

    private String sourceUrl;

    private String title;

    private String content;

    private String summary;

    private String sentiment;

    private Double sentimentScore;

    private List<String> relatedSecurities;

    private List<String> relatedIndustries;

    private List<String> tags;

    private LocalDateTime publishedAt;

    private LocalDateTime crawledAt;
}
