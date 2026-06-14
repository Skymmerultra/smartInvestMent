package com.smart.investment.module.report.consumer;

import com.rabbitmq.client.Channel;
import com.smart.investment.common.core.config.MqDeclareConfig;
import com.smart.investment.common.core.mq.BaseMessage;
import com.smart.investment.common.core.utils.JsonUtils;
import com.smart.investment.module.report.dto.ReportOcrMessage;
import com.smart.investment.module.report.service.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 财报 OCR 消费者 (T-09)
 * <p>
 * 监听队列 q.report.ocr，消费消息后执行 OCR 解析流程。
 * 使用手动确认 + 异常重试 + 死信队列兜底。
 * <p>
 * 消息由 {@link Jackson2JsonMessageConverter} 自动反序列化。
 * 由于 Java 泛型擦除，payload 需二次转换。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReportOcrConsumer {

    private final ReportService reportService;

    /**
     * 监听 q.report.ocr 队列，处理财报 OCR 解析
     *
     * @param message     消息体（Jackson2JsonMessageConverter 自动反序列化）
     * @param channel     RabbitMQ Channel（用于手动确认）
     * @param deliveryTag 投递标签
     */
    @RabbitListener(queues = MqDeclareConfig.QUEUE_REPORT_OCR)
    public void handleOcrMessage(BaseMessage<ReportOcrMessage> message, Channel channel,
                                 @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        log.info("收到 OCR 解析消息: deliveryTag={}, messageId={}", deliveryTag, message.getMessageId());
        try {
            if (message.getPayload() == null) {
                log.error("消息载荷为空, deliveryTag={}", deliveryTag);
                channel.basicAck(deliveryTag, false);
                return;
            }
            // 泛型擦除：payload 实际为 LinkedHashMap，需二次转换
            String payloadJson = JsonUtils.toJsonString(message.getPayload());
            ReportOcrMessage ocrMessage = JsonUtils.parseObject(payloadJson, ReportOcrMessage.class);

            if (ocrMessage != null) {
                reportService.processOcr(ocrMessage);
                log.info("OCR 解析处理完成: reportId={}", ocrMessage.getReportId());
            }

            // 手动 ACK
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            log.error("OCR 解析处理失败, deliveryTag={}, error={}", deliveryTag, e.getMessage(), e);
            try {
                // 拒绝消息，不重新入队（进入死信队列）
                channel.basicNack(deliveryTag, false, false);
            } catch (IOException ex) {
                log.error("消息拒绝失败, deliveryTag={}", deliveryTag, ex);
            }
        }
    }
}
