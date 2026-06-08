package com.smart.investment.ai.prompt;

import java.util.Map;

/**
 * 风险评估 Prompt (T-06)
 * <p>
 * 对投资组合或标的进行多维度风险评估。
 * 输出格式：结构化 JSON，包含风险评分、风险类别、应对建议。
 */
public class RiskAssessmentPrompt implements PromptTemplate {

    @Override
    public String buildPrompt(Map<String, Object> params) {
        String targetType = (String) params.getOrDefault("targetType", "个股");
        String targetData = (String) params.getOrDefault("targetData", "");
        String targetName = (String) params.getOrDefault("targetName", "");

        return String.format("""
                请对以下%s进行全面的风险评估分析。

                标的名称：%s
                分析数据：
                %s

                评估维度：
                1. 市场风险：
                   - Beta系数分析
                   - 波动率评估
                   - 最大回撤分析
                2. 财务风险：
                   - 偿债能力（流动比率、速动比率、资产负债率）
                   - 盈利能力稳定性
                   - 现金流健康度
                3. 行业风险：
                   - 行业周期位置
                   - 政策风险
                   - 竞争格局
                4. 流动性风险：
                   - 日均成交量
                   - 换手率
                   - 买卖价差
                5. 信用风险（如适用）

                返回标准JSON格式：
                {
                  "targetName": "标的名称",
                  "targetType": "类型",
                  "overallRiskScore": 0-100,
                  "overallRiskLevel": "高风险/中风险/低风险",
                  "riskDimensions": {
                    "marketRisk": {"score": 0-100, "level": "高/中/低", "details": "分析详情"},
                    "financialRisk": {"score": 0-100, "level": "高/中/低", "details": "分析详情"},
                    "industryRisk": {"score": 0-100, "level": "高/中/低", "details": "分析详情"},
                    "liquidityRisk": {"score": 0-100, "level": "高/中/低", "details": "分析详情"}
                  },
                  "keyRiskFactors": ["主要风险因素1", "主要风险因素2"],
                  "mitigationSuggestions": ["应对建议1", "应对建议2"],
                  "suitableInvestorProfile": "适合的投资者类型描述"
                }
                """, targetType, targetName, targetData);
    }

    @Override
    public String getTemplateName() {
        return "RiskAssessmentPrompt";
    }
}
