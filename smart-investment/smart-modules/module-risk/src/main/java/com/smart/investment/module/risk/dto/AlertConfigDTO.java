package com.smart.investment.module.risk.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertConfigDTO {

    private Long id;
    
    private String riskType;
    
    private BigDecimal varThreshold;
    
    private BigDecimal esThreshold;
    
    private String notifyMethod;
    
    private Boolean isActive;
}