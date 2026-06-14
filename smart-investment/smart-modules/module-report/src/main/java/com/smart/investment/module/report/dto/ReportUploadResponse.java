package com.smart.investment.module.report.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 财报上传响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportUploadResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 财报ID */
    private Long id;

    /** 文件路径 */
    private String fileUrl;

    /** 解析状态：0-待处理 */
    private Integer parseStatus;

    /** 提交时间 */
    private LocalDateTime createdAt;
}
