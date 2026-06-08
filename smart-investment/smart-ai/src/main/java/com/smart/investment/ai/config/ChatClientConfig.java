package com.smart.investment.ai.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;

/**
 * AI 配置 (T-06)
 * <p>
 * DeepSeekChatModel 由 spring-ai-starter-model-deepseek 自动配置，
 * 此处仅创建带金融投资分析师 System Prompt 的 ChatClient，并启用重试。
 */
@Configuration
@EnableRetry
public class ChatClientConfig {

    /**
     * ChatClient Bean
     * <p>
     * 设置默认 System Prompt 为金融投资分析师角色
     */
    @Bean
    public ChatClient chatClient(ChatClient.Builder builder) {
        return builder
                .defaultSystem("""
                        你是一位专业的金融投资分析师，拥有丰富的财务报表分析、市场趋势研判和风险管理经验。
                        请遵循以下原则：
                        1. 基于数据和事实提供分析，避免主观臆断
                        2. 分析结论需有数据支撑，引用关键指标
                        3. 对不确定的信息明确标注风险提示
                        4. 使用专业但易懂的金融术语
                        5. 返回结构化的 JSON 格式数据
                        """)
                .build();
    }
}
