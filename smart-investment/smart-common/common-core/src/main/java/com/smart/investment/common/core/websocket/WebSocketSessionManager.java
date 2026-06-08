package com.smart.investment.common.core.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket 连接管理器 (T-10)
 * <p>
 * 维护 userId → sessionId 映射，统计在线用户数，处理断线重连和会话过期。
 */
@Slf4j
@Component
public class WebSocketSessionManager {

    /** userId → sessionId 集合 */
    private final Map<String, Set<String>> userSessions = new ConcurrentHashMap<>();

    /** sessionId → userId 反向映射 */
    private final Map<String, String> sessionUsers = new ConcurrentHashMap<>();

    /**
     * 用户连接上线
     *
     * @param userId    用户ID
     * @param sessionId STOMP sessionId
     */
    public void userOnline(String userId, String sessionId) {
        userSessions.computeIfAbsent(userId, k -> ConcurrentHashMap.newKeySet()).add(sessionId);
        sessionUsers.put(sessionId, userId);
        log.info("WebSocket 用户上线: userId={}, sessionId={}, 当前在线: {}", userId, sessionId, getOnlineCount());
    }

    /**
     * 用户断开连接
     *
     * @param sessionId STOMP sessionId
     */
    public void userOffline(String sessionId) {
        String userId = sessionUsers.remove(sessionId);
        if (userId != null) {
            Set<String> sessions = userSessions.get(userId);
            if (sessions != null) {
                sessions.remove(sessionId);
                if (sessions.isEmpty()) {
                    userSessions.remove(userId);
                }
            }
            log.info("WebSocket 用户下线: userId={}, sessionId={}, 当前在线: {}", userId, sessionId, getOnlineCount());
        }
    }

    /**
     * 用户是否在线
     */
    public boolean isOnline(String userId) {
        Set<String> sessions = userSessions.get(userId);
        return sessions != null && !sessions.isEmpty();
    }

    /**
     * 获取用户的所有 sessionId
     */
    public Set<String> getUserSessions(String userId) {
        Set<String> sessions = userSessions.get(userId);
        return sessions != null ? sessions : Set.of();
    }

    /**
     * 在线用户总数（去重 userId）
     */
    public int getOnlineCount() {
        return userSessions.size();
    }

    /**
     * 获取所有在线用户ID
     */
    public Set<String> getOnlineUsers() {
        return userSessions.keySet();
    }
}
