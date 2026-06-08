package com.smart.investment.module.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录响应 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    /** 用户ID */
    private Long userId;

    /** 用户名 */
    private String username;

    /** 用户角色 */
    private String role;

    /** Access Token（有效期2小时） */
    private String accessToken;

    /** Refresh Token（有效期7天） */
    private String refreshToken;

    /** Token 类型 */
    private String tokenType;
}
