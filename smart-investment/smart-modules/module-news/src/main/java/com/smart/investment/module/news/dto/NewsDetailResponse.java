package com.smart.investment.module.news.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 新闻详情响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewsDetailResponse {

    private Long id;

    private String source;

    private String title;

    private String content;

    private String url;

    private String sentiment;

    private BigDecimal sentimentScore;

    /** 相关证券列表 */
    private List<RelatedSecurity> relatedSecurities;

    private LocalDateTime publishedAt;

    private LocalDateTime crawledAt;

    private LocalDateTime createdAt;

    /**
     * 相关证券
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RelatedSecurity {
        private String code;
        private String name;
        private String relevance;
    }
}
