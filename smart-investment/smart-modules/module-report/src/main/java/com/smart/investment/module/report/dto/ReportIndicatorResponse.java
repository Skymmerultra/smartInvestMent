package com.smart.investment.module.report.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 财务指标响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportIndicatorResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 指标ID */
    private Long id;

    /** 关联财报ID */
    private Long reportId;

    /** 指标名称 */
    private String indicatorName;

    /** 指标值 */
    private BigDecimal indicatorValue;

    /** 单位 */
    private String unit;
}
