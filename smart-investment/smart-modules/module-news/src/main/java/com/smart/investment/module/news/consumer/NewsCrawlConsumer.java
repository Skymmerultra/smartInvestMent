package com.smart.investment.module.news.consumer;

import com.rabbitmq.client.Channel;
import com.smart.investment.common.core.config.MqDeclareConfig;
import com.smart.investment.common.core.mq.BaseMessage;
import com.smart.investment.common.core.utils.JsonUtils;
import com.smart.investment.module.news.dto.NewsCrawlMessage;
import com.smart.investment.module.news.service.NewsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 新闻爬虫消费者
 * <p>
 * 监听队列 q.news.crawl，消费 Python 爬虫投递的新闻消息。
 * 流程：SimHash 去重 → 持久化 → ES 索引 → 发送情感分析消息。
 * 使用手动确认 + 异常重试 + 死信队列兜底。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NewsCrawlConsumer {

    private final NewsService newsService;

    /**
     * 监听 q.news.crawl 队列，处理爬虫投递的新闻
     *
     * @param message     消息体
     * @param channel     RabbitMQ Channel
     * @param deliveryTag 投递标签
     */
    @RabbitListener(queues = MqDeclareConfig.QUEUE_NEWS_CRAWL)
    public void handleCrawlMessage(BaseMessage<NewsCrawlMessage> message, Channel channel,
                                   @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        log.info("收到新闻爬取消息: deliveryTag={}, messageId={}", deliveryTag, message.getMessageId());
        try {
            if (message.getPayload() == null) {
                log.error("消息载荷为空, deliveryTag={}", deliveryTag);
                channel.basicAck(deliveryTag, false);
                return;
            }

            // 泛型擦除：payload 实际为 LinkedHashMap，需二次转换
            String payloadJson = JsonUtils.toJsonString(message.getPayload());
            NewsCrawlMessage crawlMessage = JsonUtils.parseObject(payloadJson, NewsCrawlMessage.class);

            if (crawlMessage != null) {
                newsService.processCrawlMessage(crawlMessage);
                log.info("新闻爬取消息处理完成: title={}", crawlMessage.getTitle());
            }

            // 手动 ACK
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            log.error("新闻爬取消息处理失败, deliveryTag={}, error={}",
                    deliveryTag, e.getMessage(), e);
            try {
                // 拒绝消息，不重新入队（进入死信队列）
                channel.basicNack(deliveryTag, false, false);
            } catch (IOException ex) {
                log.error("消息拒绝失败, deliveryTag={}", deliveryTag, ex);
            }
        }
    }
}
