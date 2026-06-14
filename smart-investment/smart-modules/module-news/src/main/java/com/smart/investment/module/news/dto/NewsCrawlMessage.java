package com.smart.investment.module.news.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Python 爬虫投递的新闻消息体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewsCrawlMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 来源（东方财富/同花顺/雪球） */
    private String source;

    /** 标题 */
    private String title;

    /** 内容 */
    private String content;

    /** 原文链接 */
    private String url;

    /** 发布时间 */
    private LocalDateTime publishedAt;
}
