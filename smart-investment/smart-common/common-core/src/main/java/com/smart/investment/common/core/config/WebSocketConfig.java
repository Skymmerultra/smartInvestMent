package com.smart.investment.common.core.config;

import com.smart.investment.common.core.websocket.AuthChannelInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket STOMP 配置 (T-10)
 * <p>
 * 配置 STOMP 端点、消息代理前缀、心跳机制、认证拦截器。
 * <p>
 * 客户端连接方式:
 * <pre>{@code
 *   // 原生 WebSocket
 *   const socket = new SockJS('http://localhost:8080/ws?token=jwt-token');
 *   const client = Stomp.over(socket);
 *
 *   // 或者 STOMP over WebSocket
 *   const client = Stomp.client('ws://localhost:8080/ws');
 *   client.connect({Authorization: 'Bearer jwt-token'}, callback);
 * }</pre>
 */
@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final AuthChannelInterceptor authChannelInterceptor;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 创建心跳调度器并显式注册（否则 simpleBrokerMessageHandler 启动失败）
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setThreadNamePrefix("ws-heartbeat-");
        scheduler.setPoolSize(1);
        scheduler.initialize();

        registry.enableSimpleBroker("/topic", "/queue")
                .setHeartbeatValue(new long[]{10000, 10000})
                .setTaskScheduler(scheduler);

        // 应用目标前缀：客户端发送消息到 /app/xxx 会路由到 @MessageMapping("/xxx")
        registry.setApplicationDestinationPrefixes("/app");

        // 用户目标前缀：/user 会被替换为 /user/{userId}（点对点）
        registry.setUserDestinationPrefix("/user");
    }

    /**
     * 配置 STOMP 端点
     * <ul>
     *   <li>/ws — STOMP 端点（支持 SockJS 降级）</li>
     *   <li>允许跨域（所有来源）</li>
     * </ul>
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                // 允许跨域（SockJS 需要）
                .setAllowedOriginPatterns("*")
                // 允许 SockJS 降级（浏览器不支持 WebSocket 时自动降级到 HTTP 流）
                .withSockJS();
    }

    /**
     * 配置客户端入站通道拦截器
     * <p>
     * CONNECT 帧经过 AuthChannelInterceptor 进行 JWT 认证
     */
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(authChannelInterceptor);
    }
}
