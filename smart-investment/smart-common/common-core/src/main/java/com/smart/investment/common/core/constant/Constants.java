package com.smart.investment.common.core.constant;

/**
 * 系统级常量
 */
public final class Constants {

    private Constants() {
        throw new UnsupportedOperationException("常量类不可实例化");
    }

    // ==================== 分页默认值 ====================

    /** 默认页码 */
    public static final int DEFAULT_PAGE = 1;

    /** 默认每页大小 */
    public static final int DEFAULT_PAGE_SIZE = 20;

    /** 最大每页大小（防止全表扫描） */
    public static final int MAX_PAGE_SIZE = 200;

    // ==================== 文件上传限制 ====================

    /** 最大文件上传大小（20MB） */
    public static final long MAX_FILE_UPLOAD_SIZE = 20 * 1024 * 1024;

    /** 允许的财报文件类型 */
    public static final String ALLOWED_REPORT_FILE_TYPE = "application/pdf";

    // ==================== Token 相关 ====================

    /** Token 黑名单前缀 */
    public static final String TOKEN_BLACKLIST_PREFIX = "token:blacklist:";

    /** 登录失败计数前缀 */
    public static final String LOGIN_FAIL_PREFIX = "login:fail:";

    // ==================== 日期 ====================

    /** 新闻数据保留天数 */
    public static final int NEWS_RETENTION_DAYS = 30;

    /** 登录失败锁定时间（分钟） */
    public static final int LOGIN_LOCK_MINUTES = 15;

    /** 登录失败最大次数 */
    public static final int LOGIN_MAX_FAIL_COUNT = 5;

    // ==================== 通用符号 ====================

    /** 逗号 */
    public static final String COMMA = ",";

    /** 点号 */
    public static final String DOT = ".";

    /** 下划线 */
    public static final String UNDERLINE = "_";

    /** 冒号 */
    public static final String COLON = ":";

    /** 空字符串 */
    public static final String EMPTY = "";

    // ==================== 系统用户角色 ====================

    /** 投资者 */
    public static final String ROLE_INVESTOR = "INVESTOR";

    /** 分析师 */
    public static final String ROLE_ANALYST = "ANALYST";

    /** 管理员 */
    public static final String ROLE_ADMIN = "ADMIN";
}
