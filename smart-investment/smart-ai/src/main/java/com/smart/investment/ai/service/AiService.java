package com.smart.investment.ai.service;

import com.smart.investment.ai.aspect.AiLog;
import com.smart.investment.ai.prompt.PromptTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * AI 调用基础服务 (T-06)
 * <p>
 * 封装 ChatClient 调用链，支持超时重试（3次，指数退避）和熔断降级。
 * 各业务模块通过 AiService 统一调用 AI，不直接依赖 ChatClient。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiService {

    private final ChatClient chatClient;

    /**
     * 调用 AI 生成结构化响应
     * <p>
     * 支持超时重试：失败后指数退避重试3次（1s → 2s → 4s），3次均失败抛异常。
     *
     * @param template     Prompt 模板
     * @param params       模板参数
     * @param responseType 期望的响应类型（DTO Class），用于 JSON 反序列化
     * @param <T>          响应类型
     * @return 反序列化后的实体
     */
    @AiLog
    @Retryable(
            retryFor = {Exception.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2, maxDelay = 10000)
    )
    public <T> T call(PromptTemplate template, Map<String, Object> params, Class<T> responseType) {
        String prompt = template.buildPrompt(params);
        log.debug("AI 调用开始: template={}", template.getTemplateName());

        return chatClient.prompt()
                .user(prompt)
                .call()
                .entity(responseType);
    }

    /**
     * 调用 AI 返回原始文本（不反序列化）
     */
    @AiLog
    @Retryable(
            retryFor = {Exception.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2, maxDelay = 10000)
    )
    public String callRaw(PromptTemplate template, Map<String, Object> params) {
        String prompt = template.buildPrompt(params);
        log.debug("AI 调用开始（原始文本）: template={}", template.getTemplateName());

        return chatClient.prompt()
                .user(prompt)
                .call()
                .content();
    }

    /**
     * 降级处理：当 AI 调用全部失败后的降级逻辑
     *
     * @param e 原始异常
     * @return 友好提示信息
     */
    @Recover
    public String fallback(Exception e, PromptTemplate template, Map<String, Object> params, Class<?> responseType) {
        log.error("AI 调用降级: template={}, error={}", template.getTemplateName(), e.getMessage());
        return "AI 服务暂时不可用，请稍后重试。错误详情：" + e.getMessage();
    }
}
