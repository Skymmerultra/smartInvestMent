package com.smart.investment.module.auth.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smart.investment.common.core.constant.Constants;
import com.smart.investment.common.core.exception.BusinessException;
import com.smart.investment.common.core.exception.ErrorCode;
import com.smart.investment.common.security.entity.SysUser;
import com.smart.investment.common.security.mapper.SysUserMapper;
import com.smart.investment.module.auth.dto.ChangePasswordRequest;
import com.smart.investment.module.auth.dto.UpdateUserRequest;
import com.smart.investment.module.auth.dto.UserDetailResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 用户管理服务
 * <p>
 * 提供用户的 CRUD、密码修改、状态管理、角色变更等业务逻辑。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final SysUserMapper sysUserMapper;
    private final PasswordEncoder passwordEncoder;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // ==================== 查询 ====================

    /**
     * 分页查询用户列表，支持按用户名搜索
     *
     * @param page    当前页码
     * @param size    每页大小
     * @param keyword 搜索关键字（用户名模糊匹配），可为 null
     * @return MyBatis-Plus 分页对象
     */
    public Page<SysUser> listUsers(int page, int size, String keyword) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            wrapper.like(SysUser::getUsername, keyword.trim());
        }
        wrapper.orderByDesc(SysUser::getCreatedAt);

        Page<SysUser> pageParam = new Page<>(page, size);
        return sysUserMapper.selectPage(pageParam, wrapper);
    }

    /**
     * 根据 ID 查询用户详情
     *
     * @param id 用户 ID
     * @return 用户详情响应
     */
    public UserDetailResponse getUserDetail(Long id) {
        SysUser user = findUserById(id);
        return toDetailResponse(user);
    }

    // ==================== 编辑 ====================

    /**
     * 编辑用户信息（用户名）
     *
     * @param id      用户 ID
     * @param request 编辑请求
     * @return 更新后的用户详情
     */
    public UserDetailResponse updateUser(Long id, UpdateUserRequest request) {
        SysUser user = findUserById(id);

        // 校验用户名唯一性（排除自己）
        if (!user.getUsername().equals(request.getUsername())) {
            Long count = sysUserMapper.selectCount(
                    new LambdaQueryWrapper<SysUser>()
                            .eq(SysUser::getUsername, request.getUsername())
            );
            if (count > 0) {
                throw new BusinessException(ErrorCode.DATA_CONFLICT, "用户名已存在");
            }
        }

        user.setUsername(request.getUsername());
        if (request.getStatus() != null) {
            user.setStatus(request.getStatus());
        }
        user.setUpdatedAt(LocalDateTime.now());
        sysUserMapper.updateById(user);

        log.info("管理员编辑用户信息 - userId: {}, username: {}", id, request.getUsername());
        return toDetailResponse(user);
    }

    /**
     * 启用/禁用用户
     *
     * @param id     用户 ID
     * @param status 0禁用 / 1启用
     */
    public void updateUserStatus(Long id, Integer status) {
        if (status != 0 && status != 1) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "状态值必须为 0（禁用）或 1（启用）");
        }

        SysUser user = findUserById(id);
        user.setStatus(status);
        user.setUpdatedAt(LocalDateTime.now());
        sysUserMapper.updateById(user);

        log.info("管理员{}用户 - userId: {}, username: {}", status == 0 ? "禁用" : "启用", id, user.getUsername());
    }

    /**
     * 变更用户角色（INVESTOR ↔ ANALYST，不允许变更为 ADMIN）
     *
     * @param id   用户 ID
     * @param role 目标角色
     */
    public void changeUserRole(Long id, String role) {
        if (!Constants.ROLE_INVESTOR.equals(role) && !Constants.ROLE_ANALYST.equals(role)) {
            throw new BusinessException(ErrorCode.PARAM_INVALID,
                    "角色仅允许变更为 INVESTOR 或 ANALYST");
        }

        SysUser user = findUserById(id);
        // 不允许修改 ADMIN 用户的角色
        if (Constants.ROLE_ADMIN.equals(user.getRole())) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "不允许变更管理员角色的用户");
        }

        user.setRole(role);
        user.setUpdatedAt(LocalDateTime.now());
        sysUserMapper.updateById(user);

        log.info("管理员变更用户角色 - userId: {}, username: {}, newRole: {}", id, user.getUsername(), role);
    }

    // ==================== 密码修改 ====================

    /**
     * 修改密码（需验证旧密码）
     *
     * @param userId  当前用户 ID
     * @param request 旧密码 + 新密码
     */
    public void changePassword(Long userId, ChangePasswordRequest request) {
        SysUser user = findUserById(userId);

        // 验证旧密码
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.PASSWORD_ERROR);
        }

        // 新旧密码不能相同
        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.DATA_VALIDATION_FAILED, "新密码不能与旧密码相同");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setUpdatedAt(LocalDateTime.now());
        sysUserMapper.updateById(user);

        log.info("用户修改密码成功 - userId: {}", userId);
    }

    // ==================== 辅助方法 ====================

    private SysUser findUserById(Long id) {
        SysUser user = sysUserMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        return user;
    }

    private UserDetailResponse toDetailResponse(SysUser user) {
        return UserDetailResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .role(user.getRole())
                .status(user.getStatus())
                .createdAt(user.getCreatedAt() != null ? user.getCreatedAt().format(FORMATTER) : null)
                .updatedAt(user.getUpdatedAt() != null ? user.getUpdatedAt().format(FORMATTER) : null)
                .build();
    }
}
