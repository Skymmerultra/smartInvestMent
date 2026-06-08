package com.smart.investment.module.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户详情响应 DTO（管理员视角）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDetailResponse {

    /** 用户ID */
    private Long id;

    /** 用户名 */
    private String username;

    /** 用户角色 */
    private String role;

    /** 状态：0禁用 / 1启用 */
    private Integer status;

    /** 创建时间 */
    private String createdAt;

    /** 更新时间 */
    private String updatedAt;
}
