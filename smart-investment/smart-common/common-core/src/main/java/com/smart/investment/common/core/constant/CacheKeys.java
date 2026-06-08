package com.smart.investment.common.core.constant;

/**
 * 缓存 Key 命名规范 (T-07)
 * <p>
 * 统一管理所有缓存 Key，使用常量避免散落硬编码。
 * 各业务模块在 Service 层通过 @Cacheable/@CacheEvict 和此常量使用缓存。
 */
public final class CacheKeys {

    private CacheKeys() {
        // 工具类，禁止实例化
    }

    /** 分隔符 */
    private static final String SEP = ":";

    /** 前缀 */
    private static final String PREFIX = "smart" + SEP;

    // ==================== 行情数据 ====================

    /** 实时行情缓存: smart:market:quote:{code} */
    public static final String MARKET_QUOTE = PREFIX + "market" + SEP + "quote" + SEP;

    public static String marketQuote(String code) {
        return MARKET_QUOTE + code;
    }

    // ==================== 新闻 ====================

    /** 最新新闻列表缓存: smart:news:latest:{page} */
    public static final String NEWS_LATEST = PREFIX + "news" + SEP + "latest" + SEP;

    public static String newsLatest(int page) {
        return NEWS_LATEST + page;
    }

    /** 新闻情感分析缓存: smart:news:sentiment:{newsId} */
    public static final String NEWS_SENTIMENT = PREFIX + "news" + SEP + "sentiment" + SEP;

    public static String newsSentiment(String newsId) {
        return NEWS_SENTIMENT + newsId;
    }

    // ==================== 风险相关 ====================

    /** 风险仪表盘缓存: smart:risk:dashboard:{userId} */
    public static final String RISK_DASHBOARD = PREFIX + "risk" + SEP + "dashboard" + SEP;

    public static String riskDashboard(String userId) {
        return RISK_DASHBOARD + userId;
    }

    /** 预警阈值缓存: smart:risk:threshold:{userId} */
    public static final String ALERT_THRESHOLD = PREFIX + "risk" + SEP + "threshold" + SEP;

    public static String alertThreshold(String userId) {
        return ALERT_THRESHOLD + userId;
    }

    // ==================== 趋势预测 ====================

    /** 趋势预测缓存: smart:trend:prediction:{code}:{period} */
    public static final String TREND_PREDICTION = PREFIX + "trend" + SEP + "prediction" + SEP;

    public static String trendPrediction(String code, String period) {
        return TREND_PREDICTION + code + SEP + period;
    }

    // ==================== 认证 ====================

    /** JWT Token 黑名单缓存: smart:auth:token:blacklist:{tokenId} */
    public static final String TOKEN_BLACKLIST = PREFIX + "auth" + SEP + "token" + SEP + "blacklist" + SEP;

    public static String tokenBlacklist(String tokenId) {
        return TOKEN_BLACKLIST + tokenId;
    }

    // ==================== 分布式锁 ====================

    /** 分布式锁前缀: smart:lock: */
    public static final String LOCK_PREFIX = PREFIX + "lock" + SEP;

    public static String lock(String lockKey) {
        return LOCK_PREFIX + lockKey;
    }
}
