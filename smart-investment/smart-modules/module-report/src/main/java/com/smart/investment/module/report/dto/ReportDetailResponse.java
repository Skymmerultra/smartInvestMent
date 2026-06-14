package com.smart.investment.module.report.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 财报详情响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportDetailResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 财报ID */
    private Long id;

    /** 公司代码 */
    private String companyCode;

    /** 公司名称 */
    private String companyName;

    /** 报告类型 */
    private String reportType;

    /** 报告期 */
    private String reportPeriod;

    /** 文件路径 */
    private String fileUrl;

    /** 解析状态：0=PENDING / 1=COMPLETED / 2=FAILED */
    private Integer parseStatus;

    /** 解析失败原因 */
    private String errorMsg;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 更新时间 */
    private LocalDateTime updatedAt;
}
