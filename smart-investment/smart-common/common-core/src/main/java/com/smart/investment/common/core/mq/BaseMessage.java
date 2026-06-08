package com.smart.investment.common.core.mq;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

/**
 * 通用消息基类 (T-09)
 * <p>
 * 所有通过 RabbitMQ 发送的消息均继承或包装此类，
 * 提供统一的消息标识、类型标记和时间戳。
 *
 * @param <T> payload 类型（泛型）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BaseMessage<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 消息唯一标识（UUID） */
    private String messageId;

    /** 消息类型（用于消费者路由分发） */
    private String type;

    /** 消息时间戳（毫秒） */
    private Long timestamp;

    /** 消息载荷（业务数据） */
    private T payload;

    // ==================== 静态工厂方法 ====================

    /**
     * 创建消息
     *
     * @param type    消息类型
     * @param payload 业务载荷
     */
    public static <T> BaseMessage<T> of(String type, T payload) {
        BaseMessage<T> message = new BaseMessage<>();
        message.messageId = UUID.randomUUID().toString();
        message.type = type;
        message.timestamp = System.currentTimeMillis();
        message.payload = payload;
        return message;
    }

    /**
     * 创建消息（指定 messageId，用于重试/幂等场景）
     */
    public static <T> BaseMessage<T> of(String messageId, String type, T payload) {
        BaseMessage<T> message = new BaseMessage<>();
        message.messageId = messageId;
        message.type = type;
        message.timestamp = System.currentTimeMillis();
        message.payload = payload;
        return message;
    }
}
