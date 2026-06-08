package com.smart.investment.module.auth.controller;

import com.smart.investment.common.core.constant.Constants;
import com.smart.investment.common.core.exception.BusinessException;
import com.smart.investment.common.core.exception.ErrorCode;
import com.smart.investment.common.core.result.Result;
import com.smart.investment.common.security.config.JwtUtils;
import com.smart.investment.common.security.entity.SysUser;
import com.smart.investment.common.security.mapper.SysUserMapper;
import com.smart.investment.module.auth.dto.LoginRequest;
import com.smart.investment.module.auth.dto.LoginResponse;
import com.smart.investment.module.auth.dto.RegisterRequest;
import com.smart.investment.module.auth.dto.UserInfoResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * 认证控制器
 * <p>
 * 提供登录、注册、登出、获取当前用户信息接口。
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final SysUserMapper sysUserMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 用户登录
     *
     * @param request 用户名 + 密码
     * @return JWT Token 和用户信息
     */
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        String failKey = Constants.LOGIN_FAIL_PREFIX + request.getUsername();

        // 1. 检查是否被锁定
        String failCountStr = stringRedisTemplate.opsForValue().get(failKey);
        if (failCountStr != null && Integer.parseInt(failCountStr) >= Constants.LOGIN_MAX_FAIL_COUNT) {
            throw new BusinessException(ErrorCode.ACCOUNT_LOCKED);
        }

        // 2. 查询用户
        SysUser user = sysUserMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getUsername, request.getUsername())
        );

        // 3. 校验用户是否存在
        if (user == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "用户名或密码错误");
        }

        // 4. 校验用户状态
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "账号已被禁用，请联系管理员");
        }

        // 5. 校验密码
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            // 密码错误，递增失败计数
            Long failCount = stringRedisTemplate.opsForValue().increment(failKey);
            if (failCount != null && failCount == 1) {
                // 首次失败，设置过期时间
                stringRedisTemplate.expire(failKey, Duration.ofMinutes(Constants.LOGIN_LOCK_MINUTES));
            }
            if (failCount != null && failCount >= Constants.LOGIN_MAX_FAIL_COUNT) {
                throw new BusinessException(ErrorCode.ACCOUNT_LOCKED);
            }
            long remaining = Constants.LOGIN_MAX_FAIL_COUNT - (failCount != null ? failCount : 0);
            throw new BusinessException(ErrorCode.UNAUTHORIZED,
                    "用户名或密码错误，还剩" + remaining + "次尝试机会");
        }

        // 6. 登录成功，清除失败计数
        stringRedisTemplate.delete(failKey);

        // 7. 生成 Token
        String accessToken = jwtUtils.generateAccessToken(user.getId(), user.getRole());
        String refreshToken = jwtUtils.generateRefreshToken(user.getId());

        // 8. 构建响应
        LoginResponse response = LoginResponse.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .role(user.getRole())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .build();

        log.info("用户登录成功 - username: {}, role: {}", user.getUsername(), user.getRole());
        return Result.success("登录成功", response);
    }

    /**
     * 用户注册
     *
     * @param request 用户名 + 密码
     * @return 注册结果
     */
    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody RegisterRequest request) {
        // 1. 校验用户名唯一性
        Long count = sysUserMapper.selectCount(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getUsername, request.getUsername())
        );
        if (count > 0) {
            throw new BusinessException(ErrorCode.DATA_CONFLICT, "用户名已存在");
        }

        // 2. 创建用户（默认 INVESTOR 角色）
        SysUser user = SysUser.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Constants.ROLE_INVESTOR)
                .status(1)
                .createdAt(LocalDateTime.now())
                .build();

        sysUserMapper.insert(user);

        log.info("用户注册成功 - username: {}, role: {}", user.getUsername(), user.getRole());
        return Result.success("注册成功", null);
    }

    /**
     * 用户登出
     * <p>
     * 将当前 Token 加入 Redis 黑名单，使之在有效期内无法再次使用。
     */
    @PostMapping("/logout")
    public Result<Void> logout(@RequestHeader("Authorization") String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            // 获取 Token 剩余有效时间，加入黑名单
            Long userId = jwtUtils.getUserIdFromExpiredToken(token);
            String blacklistKey = Constants.TOKEN_BLACKLIST_PREFIX + userId + ":" + token;
            // 黑名单保留至 Token 自然过期（最长7天）
            stringRedisTemplate.opsForValue().set(blacklistKey, "1", Duration.ofDays(7));
            log.info("用户登出 - userId: {}, token已加入黑名单", userId);
        }

        SecurityContextHolder.clearContext();
        return Result.success("登出成功", null);
    }

    /**
     * 获取当前登录用户信息
     *
     * @return 用户信息
     */
    @GetMapping("/me")
    public Result<UserInfoResponse> me() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        Long userId = (Long) authentication.getPrincipal();
        SysUser user = sysUserMapper.selectById(userId);

        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        UserInfoResponse response = UserInfoResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .role(user.getRole())
                .status(user.getStatus())
                .build();

        return Result.success(response);
    }
}
