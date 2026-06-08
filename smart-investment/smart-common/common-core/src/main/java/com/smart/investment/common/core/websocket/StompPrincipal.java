package com.smart.investment.common.core.websocket;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.security.Principal;

/**
 * STOMP 认证用户主体 (T-10)
 * <p>
 * 封装 WebSocket 连接的用户身份信息。
 */
@Getter
@AllArgsConstructor
public class StompPrincipal implements Principal {

    /** 用户ID */
    private final String userId;

    /** 用户角色 */
    private final String role;

    /** JWT Token */
    private final String token;

    @Override
    public String getName() {
        return userId;
    }
}
