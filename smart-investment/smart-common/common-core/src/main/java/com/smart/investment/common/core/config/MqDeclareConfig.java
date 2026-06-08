package com.smart.investment.common.core.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ 交换机、队列、绑定声明 (T-09)
 * <p>
 * 使用 @Bean 方式声明所有 Queue、Exchange、Binding。
 * 配置死信队列（DLX）处理消费失败消息，消息 TTL 24小时。
 *
 * <pre>
 * 交换机与队列拓扑:
 *
 *   ex.news (Direct)
 *     ├── q.news.crawl      (routingKey: news.crawl)    — Python爬虫 → Java新闻消费
 *     ├── q.news.sentiment  (routingKey: news.sentiment) — 新闻 → 情感分析
 *     └── q.news.trend      (routingKey: news.trend)     — 情感结果 → 趋势预测
 *
 *   ex.report (Direct)
 *     └── q.report.ocr      (routingKey: report.ocr)     — 财报异步OCR解析
 * </pre>
 */
@Slf4j
@Configuration
public class MqDeclareConfig {

    // ==================== 交换机 ====================

    public static final String EXCHANGE_NEWS = "ex.news";
    public static final String EXCHANGE_REPORT = "ex.report";

    // ==================== 队列 ====================

    public static final String QUEUE_NEWS_CRAWL = "q.news.crawl";
    public static final String QUEUE_NEWS_SENTIMENT = "q.news.sentiment";
    public static final String QUEUE_NEWS_TREND = "q.news.trend";
    public static final String QUEUE_REPORT_OCR = "q.report.ocr";

    // 死信队列
    public static final String DLX_EXCHANGE = "ex.dead";
    public static final String DLX_QUEUE_NEWS = "q.dead.news";
    public static final String DLX_QUEUE_REPORT = "q.dead.report";

    // ==================== 路由键 ====================

    public static final String RK_NEWS_CRAWL = "news.crawl";
    public static final String RK_NEWS_SENTIMENT = "news.sentiment";
    public static final String RK_NEWS_TREND = "news.trend";
    public static final String RK_REPORT_OCR = "report.ocr";

    // ==================== 交换机 Bean ====================

    @Bean
    public DirectExchange newsExchange() {
        return new DirectExchange(EXCHANGE_NEWS, true, false);
    }

    @Bean
    public DirectExchange reportExchange() {
        return new DirectExchange(EXCHANGE_REPORT, true, false);
    }

    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(DLX_EXCHANGE, true, false);
    }

    // ==================== 队列 Bean（带死信队列配置） ====================

    @Bean
    public Queue newsCrawlQueue() {
        return QueueBuilder.durable(QUEUE_NEWS_CRAWL)
                .withArgument("x-dead-letter-exchange", DLX_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", QUEUE_NEWS_CRAWL)
                .withArgument("x-message-ttl", 86400000) // 24小时
                .build();
    }

    @Bean
    public Queue newsSentimentQueue() {
        return QueueBuilder.durable(QUEUE_NEWS_SENTIMENT)
                .withArgument("x-dead-letter-exchange", DLX_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", QUEUE_NEWS_SENTIMENT)
                .withArgument("x-message-ttl", 86400000)
                .build();
    }

    @Bean
    public Queue newsTrendQueue() {
        return QueueBuilder.durable(QUEUE_NEWS_TREND)
                .withArgument("x-dead-letter-exchange", DLX_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", QUEUE_NEWS_TREND)
                .withArgument("x-message-ttl", 86400000)
                .build();
    }

    @Bean
    public Queue reportOcrQueue() {
        return QueueBuilder.durable(QUEUE_REPORT_OCR)
                .withArgument("x-dead-letter-exchange", DLX_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", QUEUE_REPORT_OCR)
                .withArgument("x-message-ttl", 86400000)
                .build();
    }

    // ==================== 死信队列 Bean ====================

    @Bean
    public Queue deadNewsQueue() {
        return QueueBuilder.durable(DLX_QUEUE_NEWS).build();
    }

    @Bean
    public Queue deadReportQueue() {
        return QueueBuilder.durable(DLX_QUEUE_REPORT).build();
    }

    // ==================== 绑定关系 Bean ====================

    @Bean
    public Binding bindNewsCrawl() {
        return BindingBuilder.bind(newsCrawlQueue()).to(newsExchange()).with(RK_NEWS_CRAWL);
    }

    @Bean
    public Binding bindNewsSentiment() {
        return BindingBuilder.bind(newsSentimentQueue()).to(newsExchange()).with(RK_NEWS_SENTIMENT);
    }

    @Bean
    public Binding bindNewsTrend() {
        return BindingBuilder.bind(newsTrendQueue()).to(newsExchange()).with(RK_NEWS_TREND);
    }

    @Bean
    public Binding bindReportOcr() {
        return BindingBuilder.bind(reportOcrQueue()).to(reportExchange()).with(RK_REPORT_OCR);
    }

    @Bean
    public Binding bindDeadNews() {
        return BindingBuilder.bind(deadNewsQueue()).to(deadLetterExchange()).with(QUEUE_NEWS_CRAWL);
    }

    @Bean
    public Binding bindDeadNewsSentiment() {
        return BindingBuilder.bind(deadNewsQueue()).to(deadLetterExchange()).with(QUEUE_NEWS_SENTIMENT);
    }

    @Bean
    public Binding bindDeadNewsTrend() {
        return BindingBuilder.bind(deadNewsQueue()).to(deadLetterExchange()).with(QUEUE_NEWS_TREND);
    }

    @Bean
    public Binding bindDeadReport() {
        return BindingBuilder.bind(deadReportQueue()).to(deadLetterExchange()).with(QUEUE_REPORT_OCR);
    }
}
