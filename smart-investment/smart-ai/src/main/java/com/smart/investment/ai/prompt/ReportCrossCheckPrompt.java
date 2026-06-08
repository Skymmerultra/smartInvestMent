package com.smart.investment.ai.prompt;

import java.util.Map;

/**
 * 财报交叉验证 Prompt (T-06)
 * <p>
 * 对多期财报数据进行交叉比对，发现异常波动和潜在风险。
 * 输出格式：结构化 JSON，包含异常项目、波动幅度、风险等级。
 */
public class ReportCrossCheckPrompt implements PromptTemplate {

    @Override
    public String buildPrompt(Map<String, Object> params) {
        String currentPeriodData = (String) params.getOrDefault("currentPeriodData", "");
        String previousPeriodData = (String) params.getOrDefault("previousPeriodData", "");
        String companyName = (String) params.getOrDefault("companyName", "");

        return String.format("""
                请对比分析%s的当期财报数据与上期财报数据，进行交叉验证。

                当期数据：
                %s

                上期数据：
                %s

                分析要求：
                1. 识别所有异常波动项目（变动幅度超过20%%的指标）
                2. 分析波动可能的原因（经营变化、会计政策变更、季节性因素等）
                3. 评估各异常项目的风险等级（高/中/低）
                4. 对可能存在财务造假风险的指标进行特别标注
                5. 给出综合风险评估和关注建议

                返回标准JSON格式：
                {
                  "companyName": "公司名称",
                  "overallRiskLevel": "高/中/低",
                  "abnormalItems": [
                    {
                      "itemName": "指标名称",
                      "currentValue": 当期值,
                      "previousValue": 上期值,
                      "changeRate": "变动百分比",
                      "possibleReason": "可能原因",
                      "riskLevel": "高/中/低",
                      "warningFlag": true/false
                    }
                  ],
                  "summary": "综合分析结论"
                }
                """, companyName, currentPeriodData, previousPeriodData);
    }

    @Override
    public String getTemplateName() {
        return "ReportCrossCheckPrompt";
    }
}
