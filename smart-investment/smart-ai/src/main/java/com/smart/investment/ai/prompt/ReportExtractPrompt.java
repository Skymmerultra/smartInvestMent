package com.smart.investment.ai.prompt;

import java.util.Map;

/**
 * 财报数据提取 Prompt (T-06)
 * <p>
 * 从已识别的财报文本中提取关键财务指标数据。
 * 输出格式：结构化 JSON，包含收入、利润、资产、负债等核心指标。
 */
public class ReportExtractPrompt implements PromptTemplate {

    @Override
    public String buildPrompt(Map<String, Object> params) {
        String ocrText = (String) params.getOrDefault("ocrText", "");
        String companyName = (String) params.getOrDefault("companyName", "");
        String reportPeriod = (String) params.getOrDefault("reportPeriod", "");

        return String.format("""
                请从以下%s（%s）的财报文本中提取关键财务指标数据。

                提取要求：
                1. 营业收入、营业成本、毛利润、毛利率
                2. 净利润、净利率
                3. 总资产、总负债、资产负债率
                4. 经营活动现金流净额
                5. 每股收益（EPS）
                6. 净资产收益率（ROE）
                7. 应收账款周转率、存货周转率
                8. 研发费用占比

                返回标准JSON格式：
                {
                  "companyName": "公司名称",
                  "reportPeriod": "报告期",
                  "metrics": {
                    "revenue": {"value": 数值, "unit": "亿元", "yoyGrowth": "同比增长百分比"},
                    "netProfit": {"value": 数值, "unit": "亿元", "yoyGrowth": "同比增长百分比"},
                    ...
                  }
                }

                财报文本内容：
                %s
                """, companyName, reportPeriod, ocrText);
    }

    @Override
    public String getTemplateName() {
        return "ReportExtractPrompt";
    }
}
