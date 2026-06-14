package com.smart.investment.module.report.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 财务指标实体 - 对应 fin_indicator 表
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("fin_indicator")
public class FinIndicator {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 关联财报ID */
    private Long reportId;

    /** 指标名称（如：营业收入、净利润、总资产、总负债、净资产等） */
    private String indicatorName;

    /** 指标值 */
    private BigDecimal indicatorValue;

    /** 单位（元/万元/%） */
    private String unit;

    /** 创建时间 */
    private LocalDateTime createdAt;
}
