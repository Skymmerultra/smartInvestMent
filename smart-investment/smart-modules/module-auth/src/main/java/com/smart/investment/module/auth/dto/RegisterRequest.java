package com.smart.investment.module.auth.dto;

import com.smart.investment.common.core.constant.RegexConstants;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 注册请求 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

    @NotBlank(message = "用户名不能为空")
    @Pattern(regexp = RegexConstants.USERNAME, message = "用户名格式：4-20位字母数字下划线，字母开头")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Pattern(regexp = RegexConstants.PASSWORD, message = "密码至少8位，包含大小写字母和数字")
    private String password;
}
