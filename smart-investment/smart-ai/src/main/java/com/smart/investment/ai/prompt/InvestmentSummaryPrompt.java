package com.smart.investment.ai.prompt;

import java.util.Map;

/**
 * 投资总结 Prompt (T-06)
 * <p>
 * 基于多维分析生成综合性投资总结报告。
 * 输出格式：结构化 JSON，包含投资建议、仓位建议、关键风险。
 */
public class InvestmentSummaryPrompt implements PromptTemplate {

    @Override
    public String buildPrompt(Map<String, Object> params) {
        String analysisResults = (String) params.getOrDefault("analysisResults", "");
        String investmentHorizon = (String) params.getOrDefault("investmentHorizon", "中期(3-6个月)");
        String riskTolerance = (String) params.getOrDefault("riskTolerance", "中等");

        return String.format("""
                请基于以下多维分析结果，生成一份综合投资总结报告。

                投资周期：%s
                风险偏好：%s

                分析数据（含基本面、技术面、市场、风险等多维度分析结果）：
                %s

                总结要求：
                1. 综合投资建议（买入/增持/持有/减持/卖出）
                2. 建议仓位比例（占总投资组合的百分比）
                3. 投资逻辑总结（3-5个核心要点）
                4. 催化剂事件（可能推动股价上涨的事件）
                5. 关键风险提示（可能导致亏损的风险因素）
                6. 止损和止盈建议
                7. 后续跟踪要点（需要持续关注的关键指标和事件）

                返回标准JSON格式：
                {
                  "investmentAdvice": "买入/增持/持有/减持/卖出",
                  "suggestedPosition": "建议仓位百分比",
                  "investmentLogic": ["核心逻辑1", "核心逻辑2", "核心逻辑3"],
                  "catalystEvents": [
                    {"event": "事件描述", "expectedTime": "预计时间", "impact": "正面/负面", "probability": "概率评估"}
                  ],
                  "keyRisks": [
                    {"risk": "风险描述", "severity": "高/中/低", "mitigation": "应对措施"}
                  ],
                  "stopLoss": {"price": 价格, "reason": "止损理由"},
                  "takeProfit": {"price": 价格, "reason": "止盈理由"},
                  "trackingPoints": ["跟踪要点1", "跟踪要点2", "跟踪要点3"],
                  "nextReviewDate": "建议下次复盘时间",
                  "disclaimer": "风险提示：以上分析基于公开数据和AI模型，不构成投资建议，投资有风险，入市需谨慎。"
                }
                """, investmentHorizon, riskTolerance, analysisResults);
    }

    @Override
    public String getTemplateName() {
        return "InvestmentSummaryPrompt";
    }
}
