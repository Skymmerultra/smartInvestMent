package com.smart.investment.module.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户信息响应 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoResponse {

    /** 用户ID */
    private Long id;

    /** 用户名 */
    private String username;

    /** 用户角色 */
    private String role;

    /** 状态 */
    private Integer status;
}
