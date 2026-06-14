package com.smart.investment.module.risk.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RiskAssessmentResultDTO {

    private String targetName;
    
    private String targetType;
    
    private Integer overallRiskScore;
    
    private String overallRiskLevel;
    
    private Map<String, RiskDimension> riskDimensions;
    
    private List<String> keyRiskFactors;
    
    private List<String> mitigationSuggestions;
    
    private String suitableInvestorProfile;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RiskDimension {
        private Integer score;
        private String level;
        private String details;
    }
}