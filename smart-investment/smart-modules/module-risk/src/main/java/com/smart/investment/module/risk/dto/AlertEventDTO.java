package com.smart.investment.module.risk.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertEventDTO {

    private Long id;
    
    private String riskType;
    
    private String alertLevel;
    
    private BigDecimal triggerValue;
    
    private BigDecimal thresholdValue;
    
    private String alertContent;
    
    private Boolean isRead;
    
    private LocalDateTime createdAt;
}