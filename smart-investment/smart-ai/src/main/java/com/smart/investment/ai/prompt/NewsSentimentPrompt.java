package com.smart.investment.ai.prompt;

import java.util.Map;

/**
 * 新闻情感分析 Prompt (T-06)
 * <p>
 * 对财经新闻进行情感分析，判断正面/负面/中性倾向。
 * 输出格式：结构化 JSON，包含情感标签、情感得分、相关证券列表。
 */
public class NewsSentimentPrompt implements PromptTemplate {

    @Override
    public String buildPrompt(Map<String, Object> params) {
        String title = (String) params.getOrDefault("title", "");
        String content = (String) params.getOrDefault("content", "");

        return String.format("""
                请对以下财经新闻进行情感分析。

                新闻标题：%s

                新闻内容：%s

                分析要求：
                1. 判断新闻整体情感倾向：正面(positive)、负面(negative)、中性(neutral)
                2. 给出情感得分（0-1之间，0表示极度负面，1表示极度正面，0.5表示中性）
                3. 提取新闻中提到的相关证券代码和名称
                4. 识别涉及的行业板块
                5. 用一句话总结新闻对市场可能的影响

                返回标准JSON格式：
                {
                  "sentiment": "positive/negative/neutral",
                  "sentimentScore": 0.75,
                  "relatedSecurities": [
                    {"code": "证券代码", "name": "证券名称", "relevance": "高/中/低"}
                  ],
                  "relatedIndustries": ["行业1", "行业2"],
                  "impactSummary": "一句话影响总结",
                  "keyPoints": ["要点1", "要点2"]
                }
                """, title, content);
    }

    @Override
    public String getTemplateName() {
        return "NewsSentimentPrompt";
    }
}
