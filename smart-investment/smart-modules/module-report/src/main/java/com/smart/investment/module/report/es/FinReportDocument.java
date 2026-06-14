package com.smart.investment.module.report.es;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * ES 财报文档（对应 idx_fin_report 索引）
 * <p>
 * 使用 {@link JsonNaming} 驼峰→蛇形自动映射，对齐 IndexManager 中定义的 ES mapping。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class FinReportDocument {

    /** 财报ID */
    private Long id;

    /** 公司代码 */
    private String companyCode;

    /** 公司名称 */
    private String companyName;

    /** 报告期 */
    private String reportPeriod;

    /** 报告类型 */
    private String reportType;

    /** 财报标题（生成） */
    private String title;

    /** 全文内容（ik_max_word 分词） */
    private String content;

    /** 解析状态 */
    private Integer parseStatus;

    /** 关键指标概要（JSON文本） */
    private String keyMetrics;

    /** 文件URL */
    private String fileUrl;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 更新时间 */
    private LocalDateTime updatedAt;
}
