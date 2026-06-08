package com.smart.investment.ai.prompt;

import java.util.Map;

/**
 * Prompt 模板接口 (T-06)
 * <p>
 * 定义统一的 Prompt 构建方法，各业务模块通过实现此接口定义专属的 Prompt 模板。
 * 模板负责将业务参数转换为完整的 Prompt 文本，返回格式统一为 JSON。
 */
public interface PromptTemplate {

    /**
     * 构建 Prompt 文本
     *
     * @param params 模板参数
     * @return 完整的 Prompt 文本（包含系统指令和用户输入）
     */
    String buildPrompt(Map<String, Object> params);

    /**
     * 获取模板名称（用于日志和监控）
     */
    String getTemplateName();
}
