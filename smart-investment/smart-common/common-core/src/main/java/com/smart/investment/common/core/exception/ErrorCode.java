package com.smart.investment.common.core.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 全局错误码枚举
 * <p>
 * 错误码分段规则：
 * 1xxx - 客户端参数错误
 * 2xxx - 认证授权错误
 * 3xxx - 业务逻辑错误
 * 4xxx - 资源不存在
 * 5xxx - 服务端内部错误
 */
@Getter
@AllArgsConstructor
public enum ErrorCode {

    // ==================== 通用错误 (1xxx) ====================
    /** 请求参数校验失败 */
    PARAM_INVALID(1001, "请求参数校验失败"),

    /** 请求参数缺失 */
    PARAM_MISSING(1002, "缺少必要参数"),

    /** 参数格式错误 */
    PARAM_FORMAT_ERROR(1003, "参数格式错误"),

    // ==================== 认证授权错误 (2xxx) ====================
    /** 未认证（未登录或 Token 过期） */
    UNAUTHORIZED(2001, "未登录或登录已过期，请重新登录"),

    /** 无权限访问 */
    FORBIDDEN(2003, "无权限访问该资源"),

    /** Token 无效 */
    TOKEN_INVALID(2004, "Token 无效"),

    // ==================== 业务逻辑错误 (3xxx) ====================
    /** 业务处理失败 */
    BUSINESS_ERROR(3001, "业务处理失败"),

    /** 操作不被允许 */
    OPERATION_NOT_ALLOWED(3002, "操作不被允许"),

    /** 数据冲突（如唯一键重复） */
    DATA_CONFLICT(3003, "数据冲突，已存在相同记录"),

    /** 数据校验失败 */
    DATA_VALIDATION_FAILED(3004, "数据校验失败"),

    /** 账号已被锁定 */
    ACCOUNT_LOCKED(3005, "账号已被锁定，请15分钟后再试"),

    /** 密码错误 */
    PASSWORD_ERROR(3006, "当前密码错误"),

    // ==================== 资源不存在 (4xxx) ====================
    /** 资源未找到 */
    NOT_FOUND(4004, "请求的资源不存在"),

    /** 用户不存在 */
    USER_NOT_FOUND(4101, "用户不存在"),

    /** 文件不存在 */
    FILE_NOT_FOUND(4102, "文件不存在"),

    // ==================== 服务端错误 (5xxx) ====================
    /** 服务器内部错误 */
    INTERNAL_ERROR(5000, "服务器内部错误，请稍后重试"),

    /** 服务暂不可用 */
    SERVICE_UNAVAILABLE(5003, "服务暂不可用，请稍后重试"),

    /** 数据库操作失败 */
    DATABASE_ERROR(5501, "数据库操作失败"),

    /** 文件操作失败 */
    FILE_OPERATION_ERROR(5502, "文件操作失败"),

    /** 第三方服务调用失败 */
    THIRD_PARTY_ERROR(5503, "第三方服务调用失败");

    /** 错误码 */
    private final int code;

    /** 错误描述 */
    private final String message;
}
