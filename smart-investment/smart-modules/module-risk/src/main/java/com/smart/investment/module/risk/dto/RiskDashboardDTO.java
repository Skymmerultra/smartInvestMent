package com.smart.investment.module.risk.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RiskDashboardDTO {

    private Long portfolioId;
    
    private BigDecimal totalVar;
    
    private BigDecimal totalEs;
    
    private RiskOverview marketRisk;
    
    private RiskOverview creditRisk;
    
    private RiskOverview liquidityRisk;
    
    private Integer alertCount;
    
    private LocalDateTime lastUpdateTime;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RiskOverview {
        private String riskType;
        private BigDecimal varValue;
        private BigDecimal esValue;
        private BigDecimal threshold;
        private String status;
        private String level;
    }
}