package com.smart.investment.module.news.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 情感分析 MQ 消息体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewsSentimentMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 新闻ID */
    private Long newsId;
}
