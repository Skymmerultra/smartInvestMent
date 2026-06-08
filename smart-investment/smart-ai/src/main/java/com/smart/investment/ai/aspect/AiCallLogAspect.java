package com.smart.investment.ai.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

/**
 * AI 调用日志切面 (T-06)
 * <p>
 * 记录每次 AI 调用的响应时间和成功/失败状态。
 * 拦截所有标注 @AiLog 的方法。
 */
@Slf4j
@Aspect
@Component
public class AiCallLogAspect {

    /**
     * 环绕通知：记录 AI 调用的耗时、方法和结果
     */
    @Around("@annotation(com.smart.investment.ai.aspect.AiLog)")
    public Object logAiCall(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String methodName = signature.getMethod().getName();
        String className = signature.getDeclaringType().getSimpleName();

        long startTime = System.currentTimeMillis();
        try {
            Object result = joinPoint.proceed();
            long elapsed = System.currentTimeMillis() - startTime;

            log.info("[AI调用] 成功 | 类: {} | 方法: {} | 耗时: {}ms | 响应时间: {}",
                    className, methodName, elapsed, formatDuration(elapsed));

            return result;
        } catch (Exception e) {
            long elapsed = System.currentTimeMillis() - startTime;

            log.error("[AI调用] 失败 | 类: {} | 方法: {} | 耗时: {}ms | 错误: {}",
                    className, methodName, elapsed, e.getMessage());

            throw e;
        }
    }

    /**
     * 格式化耗时
     */
    private String formatDuration(long millis) {
        if (millis < 1000) {
            return millis + "ms";
        }
        if (millis < 60000) {
            return String.format("%.1fs", millis / 1000.0);
        }
        long minutes = millis / 60000;
        long seconds = (millis % 60000) / 1000;
        return String.format("%dm%ds", minutes, seconds);
    }
}
