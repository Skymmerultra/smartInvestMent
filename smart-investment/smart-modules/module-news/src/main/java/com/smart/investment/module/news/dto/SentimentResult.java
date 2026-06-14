package com.smart.investment.module.news.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * AI 情感分析结果 - 对应 NewsSentimentPrompt 输出 JSON
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SentimentResult {

    /** 情感倾向：positive / negative / neutral */
    private String sentiment;

    /** 情感得分（0~1） */
    private Double sentimentScore;

    /** 相关证券列表 */
    private List<RelatedSecurity> relatedSecurities;

    /** 涉及行业 */
    private List<String> relatedIndustries;

    /** 一句话影响总结 */
    private String impactSummary;

    /** 关键要点 */
    private List<String> keyPoints;

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
