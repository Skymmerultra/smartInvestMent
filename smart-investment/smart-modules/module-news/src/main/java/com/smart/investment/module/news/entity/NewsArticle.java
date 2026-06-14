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
 * 新闻资讯实体 - 对应 news_article 表
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("news_article")
public class NewsArticle {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 来源（东方财富/同花顺/雪球） */
    private String source;

    /** 标题 */
    private String title;

    /** 内容 */
    private String content;

    /** 原文链接 */
    private String url;

    /** 情感倾向：POSITIVE / NEGATIVE / NEUTRAL */
    private String sentiment;

    /** 情感得分（0~1） */
    private BigDecimal sentimentScore;

    /** 相关证券列表（JSON） */
    @TableField("related_securities")
    private String relatedSecurities;

    /** 发布时间 */
    private LocalDateTime publishedAt;

    /** 爬取时间 */
    private LocalDateTime crawledAt;

    /** 创建时间 */
    private LocalDateTime createdAt;
}
