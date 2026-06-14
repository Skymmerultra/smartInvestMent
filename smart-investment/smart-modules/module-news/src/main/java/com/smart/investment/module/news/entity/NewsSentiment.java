package com.smart.investment.module.news.entity;

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

/**
 * 新闻情感标签实体 - 对应 news_sentiment 表
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("news_sentiment")
public class NewsSentiment {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 关联新闻ID */
    private Long newsId;

    /** 情感标签：POSITIVE / NEGATIVE / NEUTRAL */
    @TableField("sentiment_label")
    private String sentimentLabel;

    /** 情感得分（0~1） */
    private BigDecimal sentimentScore;

    /** 相关证券代码 */
    @TableField("related_security")
    private String relatedSecurity;

    /** 分析模型 */
    @TableField("ai_model")
    private String aiModel;

    /** 分析时间 */
    private LocalDateTime analyzedAt;
}
