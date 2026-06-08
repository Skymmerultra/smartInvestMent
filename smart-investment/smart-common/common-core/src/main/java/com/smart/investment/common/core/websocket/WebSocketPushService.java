package com.smart.investment.common.core.websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

/**
 * 通用 WebSocket 推送服务 (T-10)
 * <p>
 * 提供点对点推送和广播推送能力。
 * 预定义推送主题供各业务模块使用。
 * <p>
 * 使用方式:
 * <pre>{@code
 *   // 广播推送
 *   webSocketPushService.pushToTopic("/topic/risk-alert", alertData);
 *
 *   // 点对点推送
 *   webSocketPushService.pushToUser("userId", "/queue/alert", message);
 * }</pre>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WebSocketPushService {

    private final SimpMessagingTemplate messagingTemplate;

    // ==================== 预定义推送主题 ====================

    /** 风险预警通知广播 */
    public static final String TOPIC_RISK_ALERT = "/topic/risk-alert";

    /** 新闻实时推送广播 */
    public static final String TOPIC_NEWS_FEED = "/topic/news-feed";

    /** 个人预警消息队列 */
    public static final String USER_QUEUE_ALERT = "/queue/alert";

    /** 个人通知队列 */
    public static final String USER_QUEUE_NOTIFICATION = "/queue/notification";

    // ==================== 推送方法 ====================

    /**
     * 广播推送（发送到 /topic 前缀的目标）
     *
     * @param destination 目标主题路径（如 "/topic/risk-alert"）
     * @param payload     推送消息载荷
     */
    public void pushToTopic(String destination, Object payload) {
        messagingTemplate.convertAndSend(destination, payload);
        log.debug("WebSocket 广播推送: destination={}", destination);
    }

    /**
     * 点对点推送（发送给指定用户）
     *
     * @param userId      目标用户ID
     * @param destination 目标路径（如 "/queue/alert"，注意不带 /user 前缀）
     * @param payload     推送消息载荷
     */
    public void pushToUser(String userId, String destination, Object payload) {
        // SimpMessagingTemplate.convertAndSendToUser 自动添加 /user/{userId} 前缀
        messagingTemplate.convertAndSendToUser(userId, destination, payload);
        log.debug("WebSocket 点对点推送: userId={}, destination={}", userId, destination);
    }

    // ==================== 便捷推送方法 ====================

    /**
     * 推送风险预警通知（广播）
     */
    public void pushRiskAlert(Object payload) {
        pushToTopic(TOPIC_RISK_ALERT, payload);
    }

    /**
     * 推送实时新闻（广播）
     */
    public void pushNewsFeed(Object payload) {
        pushToTopic(TOPIC_NEWS_FEED, payload);
    }

    /**
     * 推送个人预警消息（点对点）
     */
    public void pushPersonalAlert(String userId, Object payload) {
        pushToUser(userId, USER_QUEUE_ALERT, payload);
    }

    /**
     * 推送个人通知（点对点）
     */
    public void pushNotification(String userId, Object payload) {
        pushToUser(userId, USER_QUEUE_NOTIFICATION, payload);
    }
}
