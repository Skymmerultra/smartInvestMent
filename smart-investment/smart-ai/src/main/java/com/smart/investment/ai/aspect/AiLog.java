package com.smart.investment.ai.aspect;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * AI 调用日志注解 (T-06)
 * <p>
 * 标注在方法上，通过 AOP 切面自动记录 AI 调用的 Token 消耗和响应时间。
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AiLog {

    /**
     * 调用场景描述（用于日志标识）
     */
    String value() default "";
}
