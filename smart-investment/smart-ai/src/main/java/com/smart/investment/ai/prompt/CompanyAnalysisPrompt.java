package com.smart.investment.ai.prompt;

import java.util.Map;

/**
 * 公司综合分析 Prompt (T-06)
 * <p>
 * 对上市公司进行基本面综合分析，涵盖财务、估值、成长性等。
 * 输出格式：结构化 JSON，包含综合评分和各维度得分。
 */
public class CompanyAnalysisPrompt implements PromptTemplate {

    @Override
    public String buildPrompt(Map<String, Object> params) {
        String companyName = (String) params.getOrDefault("companyName", "");
        String companyCode = (String) params.getOrDefault("companyCode", "");
        String financialData = (String) params.getOrDefault("financialData", "");
        String marketData = (String) params.getOrDefault("marketData", "");

        return String.format("""
                请对以下公司进行全面的基本面分析。

                公司名称：%s
                证券代码：%s

                财务数据：
                %s

                市场数据：
                %s

                分析维度：
                1. 公司概况与商业模式
                2. 财务健康度：
                   - 盈利能力（ROE、毛利率、净利率趋势）
                   - 成长能力（营收增长率、净利润增长率、CAGR）
                   - 偿债能力（流动比率、速动比率、资产负债率）
                   - 运营效率（存货周转、应收账款周转）
                3. 估值分析：
                   - 市盈率（PE）、市净率（PB）、市销率（PS）
                   - 与行业均值对比
                   - 历史估值分位数
                4. 竞争优势（护城河分析）
                5. 公司治理评估
                6. 成长驱动力分析

                返回标准JSON格式：
                {
                  "companyName": "公司名称",
                  "companyCode": "证券代码",
                  "overallScore": 0-100,
                  "overallRating": "强烈推荐/推荐/中性/回避/强烈回避",
                  "businessModel": "商业模式描述",
                  "dimensions": {
                    "profitability": {"score": 0-100, "description": "盈利能力说明", "keyMetrics": {}},
                    "growth": {"score": 0-100, "description": "成长能力说明", "keyMetrics": {}},
                    "financialHealth": {"score": 0-100, "description": "财务健康说明", "keyMetrics": {}},
                    "valuation": {"score": 0-100, "description": "估值分析说明", "keyMetrics": {"pe": 数值, "pb": 数值, "industryPe": 数值}},
                    "competitiveAdvantage": {"score": 0-100, "description": "竞争优势说明"},
                    "governance": {"score": 0-100, "description": "治理评估"}
                  },
                  "moatAnalysis": "护城河分析",
                  "growthDrivers": ["增长驱动1", "增长驱动2"],
                  "keyRisks": ["风险1", "风险2"],
                  "targetPriceRange": {"low": 价格, "high": 价格, "methodology": "估值方法说明"}
                }
                """, companyName, companyCode, financialData, marketData);
    }

    @Override
    public String getTemplateName() {
        return "CompanyAnalysisPrompt";
    }
}
