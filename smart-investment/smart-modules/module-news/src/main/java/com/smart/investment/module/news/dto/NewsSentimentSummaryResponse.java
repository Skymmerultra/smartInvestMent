package com.smart.investment.module.news.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 新闻情感分析汇总响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewsSentimentSummaryResponse {

    /** 新闻总数 */
    private long totalCount;

    /** 按来源聚合统计 */
    private List<SourceSummary> bySource;

    /** 按日期聚合统计 */
    private List<DateSummary> byDate;

    /**
     * 按来源的情感统计
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SourceSummary {
        private String source;
        private long positiveCount;
        private long negativeCount;
        private long neutralCount;
        private long totalCount;
    }

    /**
     * 按日期的情感统计
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DateSummary {
        private String date;
        private long positiveCount;
        private long negativeCount;
        private long neutralCount;
        private long totalCount;
    }
}
