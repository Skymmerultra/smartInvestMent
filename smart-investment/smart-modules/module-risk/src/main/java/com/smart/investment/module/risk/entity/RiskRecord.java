package com.smart.investment.module.risk.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
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
@TableName("risk_record")
public class RiskRecord {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("portfolio_id")
    private Long portfolioId;

    @TableField("risk_type")
    private String riskType;

    @TableField("var_value")
    private BigDecimal varValue;

    @TableField("es_value")
    private BigDecimal esValue;

    @TableField("threshold")
    private BigDecimal threshold;

    @TableField("is_alerted")
    private Integer isAlerted;

    @TableField("created_at")
    private LocalDateTime createdAt;
}