package com.smart.investment.module.auth.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smart.investment.common.core.constant.Constants;
import com.smart.investment.common.core.result.PageResult;
import com.smart.investment.common.core.result.Result;
import com.smart.investment.common.security.entity.SysUser;
import com.smart.investment.module.auth.dto.ChangePasswordRequest;
import com.smart.investment.module.auth.dto.UpdateUserRequest;
import com.smart.investment.module.auth.dto.UserDetailResponse;
import com.smart.investment.module.auth.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 用户管理控制器
 * <p>
 * 管理员专属接口，提供用户 CRUD、状态管理、角色变更等功能。
 * 普通用户的密码修改接口也在此控制器中。
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // ==================== 管理员接口 ====================

    /**
     * 管理员查看用户列表（分页、搜索）
     */
    @GetMapping("/api/admin/users")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<PageResult<UserDetailResponse>> listUsers(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword) {

        // 限制最大每页条数
        if (size > Constants.MAX_PAGE_SIZE) {
            size = Constants.MAX_PAGE_SIZE;
        }

        Page<SysUser> userPage = userService.listUsers(page, size, keyword);

        List<UserDetailResponse> records = userPage.getRecords().stream()
                .map(this::toDetailResponse)
                .collect(Collectors.toList());

        PageResult<UserDetailResponse> pageResult = PageResult.of(
                records, userPage.getTotal(), userPage.getCurrent(), userPage.getSize());

        return Result.success(pageResult);
    }

    /**
     * 管理员查看用户详情
     */
    @GetMapping("/api/admin/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<UserDetailResponse> getUserDetail(@PathVariable Long id) {
        UserDetailResponse detail = userService.getUserDetail(id);
        return Result.success(detail);
    }

    /**
     * 管理员编辑用户信息
     */
    @PutMapping("/api/admin/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<UserDetailResponse> updateUser(@PathVariable Long id,
                                                  @Valid @RequestBody UpdateUserRequest request) {
        UserDetailResponse updated = userService.updateUser(id, request);
        return Result.success("用户信息已更新", updated);
    }

    /**
     * 管理员启用/禁用用户
     */
    @PutMapping("/api/admin/users/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> updateUserStatus(@PathVariable Long id,
                                          @RequestBody Map<String, Integer> body) {
        Integer status = body.get("status");
        if (status == null) {
            return Result.error(400, "缺少 status 参数");
        }
        userService.updateUserStatus(id, status);
        return Result.success(status == 1 ? "用户已启用" : "用户已禁用", null);
    }

    /**
     * 管理员变更用户角色
     */
    @PutMapping("/api/admin/users/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> changeUserRole(@PathVariable Long id,
                                        @RequestBody Map<String, String> body) {
        String role = body.get("role");
        if (role == null || role.isBlank()) {
            return Result.error(400, "缺少 role 参数");
        }
        userService.changeUserRole(id, role.toUpperCase());
        return Result.success("角色变更成功", null);
    }

    // ==================== 当前用户接口 ====================

    /**
     * 当前用户修改密码（需验证旧密码）
     */
    @PutMapping("/api/auth/password")
    public Result<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        Long userId = getCurrentUserId();
        userService.changePassword(userId, request);
        return Result.success("密码修改成功", null);
    }

    // ==================== 辅助方法 ====================

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new com.smart.investment.common.core.exception.BusinessException(
                    com.smart.investment.common.core.exception.ErrorCode.UNAUTHORIZED);
        }
        return (Long) authentication.getPrincipal();
    }

    private UserDetailResponse toDetailResponse(SysUser user) {
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return UserDetailResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .role(user.getRole())
                .status(user.getStatus())
                .createdAt(user.getCreatedAt() != null ? user.getCreatedAt().format(formatter) : null)
                .updatedAt(user.getUpdatedAt() != null ? user.getUpdatedAt().format(formatter) : null)
                .build();
    }
}
