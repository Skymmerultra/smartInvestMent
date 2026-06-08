package com.smart.investment.common.core.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ 连接配置 (T-09)
 * <p>
 * 配置 RabbitTemplate（开启发送确认、返回回调、JSON 消息转换）。
 * 消息可靠性保障：生产者确认、消费者手动确认、重试机制（在 application.yml 中配置）。
 */
@Slf4j
@Configuration
public class RabbitMQConfig {

    /**
     * JSON 消息转换器（替代默认的 SimpleMessageConverter）
     */
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * RabbitTemplate — 配置确认回调和消息转换
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter messageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);

        // 生产者确认：消息成功到达 Exchange
        template.setConfirmCallback((correlationData, ack, cause) -> {
            if (ack) {
                log.debug("消息发送确认成功: id={}", correlationData != null ? correlationData.getId() : "null");
            } else {
                log.error("消息发送确认失败: id={}, cause={}",
                        correlationData != null ? correlationData.getId() : "null", cause);
            }
        });

        // 路由失败回调：消息无法路由到队列时
        template.setReturnsCallback(returned -> {
            log.error("消息路由失败: exchange={}, routingKey={}, replyCode={}, replyText={}, message={}",
                    returned.getExchange(), returned.getRoutingKey(),
                    returned.getReplyCode(), returned.getReplyText(),
                    new String(returned.getMessage().getBody()));
        });

        // 必须设为 true 才能触发 returnsCallback
        template.setMandatory(true);

        return template;
    }
}
