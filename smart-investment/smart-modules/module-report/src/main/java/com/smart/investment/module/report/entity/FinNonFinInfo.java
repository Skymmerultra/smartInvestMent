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
 * 非财务信息实体 - 对应 fin_non_fin_info 表
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("fin_non_fin_info")
public class FinNonFinInfo {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 关联财报ID */
    private Long reportId;

    /** 信息类型（BUSINESS_STRATEGY / NEW_PRODUCT / MANAGEMENT_CHANGE 等） */
    @TableField("info_type")
    private String infoType;

    /** 信息内容 */
    @TableField("info_content")
    private String infoContent;

    /** 创建时间 */
    private LocalDateTime createdAt;
}
