package com.smart.investment.module.report.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 财报实体 - 对应 fin_report 表
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("fin_report")
public class FinReport {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 公司代码 */
    private String companyCode;

    /** 公司名称 */
    private String companyName;

    /** 报告类型：ANNUAL / QUARTER / MONTHLY */
    private String reportType;

    /** 报告期（如 2025Q4） */
    private String reportPeriod;

    /** 原文件URL */
    private String fileUrl;

    /** 解析状态：0=PENDING / 1=COMPLETED / 2=FAILED */
    private Integer parseStatus;

    /** 解析失败原因 */
    @TableField("error_msg")
    private String errorMsg;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 更新时间 */
    private LocalDateTime updatedAt;

    // ==================== 解析状态常量 ====================

    public static final int STATUS_PENDING = 0;
    public static final int STATUS_COMPLETED = 1;
    public static final int STATUS_FAILED = 2;
}
