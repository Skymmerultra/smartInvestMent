package com.smart.investment.module.report.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 财报 OCR 消息载荷（MQ 传输）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportOcrMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 财报ID */
    private Long reportId;

    /** 文件路径 */
    private String fileUrl;

    /** 公司名称 */
    private String companyName;

    /** 公司代码 */
    private String companyCode;

    /** 报告期 */
    private String reportPeriod;
}
