package com.smart.investment.common.core.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Spring Cache 配置 (T-07)
 * <p>
 * 启用 @EnableCaching，配置基于 Redis 的 CacheManager，
 * 为不同缓存区域设置不同的 TTL。
 */
@Configuration
@EnableCaching
public class CacheConfig implements CachingConfigurer {

    /**
     * 各缓存区域默认过期时间
     */
    public static final Duration TTL_MARKET_QUOTE = Duration.ofSeconds(60);       // 行情数据 60s
    public static final Duration TTL_NEWS_LIST = Duration.ofSeconds(300);        // 新闻列表 300s
    public static final Duration TTL_NEWS_SENTIMENT = Duration.ofSeconds(300);   // 新闻情感 300s
    public static final Duration TTL_RISK_DASHBOARD = Duration.ofSeconds(600);   // 风险仪表盘 600s
    public static final Duration TTL_ALERT_THRESHOLD = Duration.ofSeconds(600);  // 预警阈值 600s
    public static final Duration TTL_TREND_PREDICTION = Duration.ofSeconds(600); // 趋势预测 600s
    public static final Duration TTL_TOKEN_BLACKLIST = Duration.ofHours(2);      // Token 黑名单 2h
    public static final Duration TTL_DEFAULT = Duration.ofMinutes(5);            // 默认 5min

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        // 默认缓存配置
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(TTL_DEFAULT)
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(RedisSerializer.string()))
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(RedisSerializer.json()))
                .disableCachingNullValues();

        // 各缓存区域自定义 TTL
        Map<String, RedisCacheConfiguration> cacheConfigs = new HashMap<>();
        cacheConfigs.put("marketQuote", defaultConfig.entryTtl(TTL_MARKET_QUOTE));
        cacheConfigs.put("newsList", defaultConfig.entryTtl(TTL_NEWS_LIST));
        cacheConfigs.put("newsSentiment", defaultConfig.entryTtl(TTL_NEWS_SENTIMENT));
        cacheConfigs.put("riskDashboard", defaultConfig.entryTtl(TTL_RISK_DASHBOARD));
        cacheConfigs.put("alertThreshold", defaultConfig.entryTtl(TTL_ALERT_THRESHOLD));
        cacheConfigs.put("trendPrediction", defaultConfig.entryTtl(TTL_TREND_PREDICTION));
        cacheConfigs.put("tokenBlacklist", defaultConfig.entryTtl(TTL_TOKEN_BLACKLIST));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigs)
                .build();
    }

    /**
     * 默认 Key 生成器：类名:方法名:参数值
     */
    @Override
    public KeyGenerator keyGenerator() {
        return (target, method, params) -> {
            StringBuilder sb = new StringBuilder();
            sb.append(target.getClass().getSimpleName());
            sb.append(":");
            sb.append(method.getName());
            sb.append(":");
            for (Object param : params) {
                if (param != null) {
                    sb.append(param.toString());
                }
            }
            return sb.toString();
        };
    }
}
