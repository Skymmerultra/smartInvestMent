package com.smart.investment.common.core.websocket;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.List;

/**
 * WebSocket 通道拦截器 (T-10)
 * <p>
 * 拦截 STOMP CONNECT 帧进行 JWT 认证：
 * 1. 从查询参数或 Header 提取 Token
 * 2. 验证 Token 有效性
 * 3. 未认证用户拒绝连接
 * <p>
 * Token 传递方式：
 * - STOMP 握手 URL 查询参数: ws://host/ws?token=xxx
 * - STOMP CONNECT 帧 Header: Authorization: Bearer xxx
 */
@Slf4j
@Component
public class AuthChannelInterceptor implements ChannelInterceptor {

    @Value("${jwt.secret}")
    private String jwtSecret;

    private static final String TOKEN_PARAM = "token";
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            String token = extractToken(accessor);

            if (!StringUtils.hasText(token)) {
                log.warn("WebSocket 连接被拒绝: 未携带 Token");
                throw new IllegalArgumentException("未携带有效的认证 Token");
            }

            try {
                Claims claims = parseToken(token);
                String userId = claims.getSubject();
                String role = claims.get("role", String.class);

                // 将用户信息设置为 Principal（后续可通过 @AuthenticationPrincipal 获取）
                Principal principal = new StompPrincipal(userId, role, token);
                accessor.setUser(principal);

                log.debug("WebSocket 认证成功: userId={}, role={}", userId, role);
            } catch (Exception e) {
                log.warn("WebSocket 连接被拒绝: Token 无效 - {}", e.getMessage());
                throw new IllegalArgumentException("Token 无效或已过期");
            }
        }

        return message;
    }

    /**
     * 从 STOMP 握手请求中提取 Token
     */
    private String extractToken(StompHeaderAccessor accessor) {
        // 方式1：从 CONNECT 帧 Header 的 Authorization 提取
        List<String> authHeaders = accessor.getNativeHeader(AUTHORIZATION_HEADER);
        if (authHeaders != null && !authHeaders.isEmpty()) {
            String auth = authHeaders.get(0);
            if (auth.startsWith(BEARER_PREFIX)) {
                return auth.substring(BEARER_PREFIX.length());
            }
        }

        // 方式2：从握手 URL 的查询参数中提取
        String query = accessor.getFirstNativeHeader(TOKEN_PARAM);
        if (StringUtils.hasText(query)) {
            return query;
        }

        return null;
    }

    /**
     * 解析并验证 JWT Token
     */
    private Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(getSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }
}
