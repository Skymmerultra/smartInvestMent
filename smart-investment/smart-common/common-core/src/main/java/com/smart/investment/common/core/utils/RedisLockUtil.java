package com.smart.investment.common.core.utils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Redis 分布式锁工具 (T-07)
 * <p>
 * 基于 SET NX EX 实现，支持自动续期和超时释放。
 * 用于定时任务防重执行、并发场景下的资源保护。
 * <p>
 * 使用方式:
 * <pre>{@code
 *   String lockId = redisLockUtil.tryLock("taskKey", 30, TimeUnit.SECONDS);
 *   if (lockId != null) {
 *       try {
 *           // 执行业务逻辑
 *       } finally {
 *           redisLockUtil.unlock("taskKey", lockId);
 *       }
 *   }
 * }</pre>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedisLockUtil {

    private final StringRedisTemplate stringRedisTemplate;

    /** 释放锁的 Lua 脚本：仅当 value 匹配时才删除 */
    private static final String UNLOCK_SCRIPT =
            "if redis.call('get', KEYS[1]) == ARGV[1] then " +
            "    return redis.call('del', KEYS[1]) " +
            "else " +
            "    return 0 " +
            "end";

    /**
     * 尝试获取分布式锁
     *
     * @param lockKey  锁的 Key（会自动加 smart:lock: 前缀）
     * @param timeout  锁超时时间
     * @param unit     时间单位
     * @return 锁的唯一标识（用于释放），获取失败返回 null
     */
    public String tryLock(String lockKey, long timeout, TimeUnit unit) {
        String key = "smart:lock:" + lockKey;
        String lockId = UUID.randomUUID().toString();
        Boolean acquired = stringRedisTemplate.opsForValue()
                .setIfAbsent(key, lockId, Duration.ofMillis(unit.toMillis(timeout)));
        if (Boolean.TRUE.equals(acquired)) {
            log.debug("获取分布式锁成功: key={}, lockId={}", key, lockId);
            return lockId;
        }
        log.debug("获取分布式锁失败（已被占用）: key={}", key);
        return null;
    }

    /**
     * 释放分布式锁（仅释放自己持有的锁）
     *
     * @param lockKey 锁的 Key
     * @param lockId  获取锁时返回的唯一标识
     */
    public void unlock(String lockKey, String lockId) {
        if (lockId == null) {
            return;
        }
        String key = "smart:lock:" + lockKey;
        Long result = stringRedisTemplate.execute(
                new org.springframework.data.redis.core.script.DefaultRedisScript<>(
                        UNLOCK_SCRIPT, Long.class),
                java.util.Collections.singletonList(key),
                lockId);
        if (result != null && result > 0) {
            log.debug("释放分布式锁成功: key={}, lockId={}", key, lockId);
        } else {
            log.debug("释放分布式锁失败（锁不存在或不属于当前持有者）: key={}, lockId={}", key, lockId);
        }
    }

    /**
     * 检查锁是否被持有
     *
     * @param lockKey 锁的 Key
     * @return true=锁存在（被持有）
     */
    public boolean isLocked(String lockKey) {
        String key = "smart:lock:" + lockKey;
        return Boolean.TRUE.equals(stringRedisTemplate.hasKey(key));
    }
}
