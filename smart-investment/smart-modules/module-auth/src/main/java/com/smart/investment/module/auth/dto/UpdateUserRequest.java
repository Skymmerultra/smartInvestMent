package com.smart.investment.module.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import static com.smart.investment.common.core.constant.RegexConstants.USERNAME;

/**
 * 管理员编辑用户请求 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserRequest {

    @NotBlank(message = "用户名不能为空")
    @Pattern(regexp = USERNAME, message = "用户名格式：4-20位字母数字下划线，字母开头")
    private String username;

    /** 状态：0禁用 / 1启用 */
    private Integer status;
}
