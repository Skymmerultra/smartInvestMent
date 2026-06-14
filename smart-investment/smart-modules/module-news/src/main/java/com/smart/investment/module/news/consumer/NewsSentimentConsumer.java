package com.smart.investment.module.news.consumer;

import com.rabbitmq.client.Channel;
import com.smart.investment.common.core.config.MqDeclareConfig;
import com.smart.investment.common.core.mq.BaseMessage;
import com.smart.investment.common.core.utils.JsonUtils;
import com.smart.investment.module.news.dto.NewsSentimentMessage;
import com.smart.investment.module.news.service.NewsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 新闻情感分析消费者
 * <p>
 * 监听队列 q.news.sentiment，消费情感分析任务。
 * 流程：调用 AI 情感分析 → 更新 news_article 情感字段 → 写入 news_sentiment → 发送趋势消息。
 * 使用手动确认 + 异常重试 + 死信队列兜底。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NewsSentimentConsumer {

    private final NewsService newsService;

    /**
     * 监听 q.news.sentiment 队列，处理情感分析
     *
     * @param message     消息体
     * @param channel     RabbitMQ Channel
     * @param deliveryTag 投递标签
     */
    @RabbitListener(queues = MqDeclareConfig.QUEUE_NEWS_SENTIMENT)
    public void handleSentimentMessage(BaseMessage<NewsSentimentMessage> message, Channel channel,
                                       @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        log.info("收到情感分析消息: deliveryTag={}, messageId={}", deliveryTag, message.getMessageId());
        try {
            if (message.getPayload() == null) {
                log.error("消息载荷为空, deliveryTag={}", deliveryTag);
                channel.basicAck(deliveryTag, false);
                return;
            }

            // 泛型擦除：payload 实际为 LinkedHashMap，需二次转换
            String payloadJson = JsonUtils.toJsonString(message.getPayload());
            NewsSentimentMessage sentimentMessage =
                    JsonUtils.parseObject(payloadJson, NewsSentimentMessage.class);

            if (sentimentMessage != null && sentimentMessage.getNewsId() != null) {
                newsService.processSentiment(sentimentMessage.getNewsId());
                log.info("情感分析处理完成: newsId={}", sentimentMessage.getNewsId());
            } else {
                log.warn("情感分析消息中 newsId 为空, deliveryTag={}", deliveryTag);
            }

            // 手动 ACK
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            log.error("情感分析处理失败, deliveryTag={}, error={}",
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
