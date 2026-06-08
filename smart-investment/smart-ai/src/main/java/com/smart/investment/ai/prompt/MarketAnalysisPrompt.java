package com.smart.investment.ai.prompt;

import java.util.Map;

/**
 * 市场分析 Prompt (T-06)
 * <p>
 * 基于市场数据和新闻进行综合性市场分析。
 * 输出格式：结构化 JSON，包含市场概况、板块轮动、资金流向分析。
 */
public class MarketAnalysisPrompt implements PromptTemplate {

    @Override
    public String buildPrompt(Map<String, Object> params) {
        String marketData = (String) params.getOrDefault("marketData", "");
        String analysisDate = (String) params.getOrDefault("analysisDate", "");

        return String.format("""
                请基于以下市场数据（%s）进行综合性市场分析。

                市场数据：
                %s

                分析维度：
                1. 市场总体概况：
                   - 主要指数表现（上证、深证、创业板、科创50等）
                   - 市场成交量与成交额分析
                   - 涨跌家数比和赚钱效应
                2. 板块分析：
                   - 当日领涨和领跌板块
                   - 板块轮动趋势判断
                   - 资金流入流出TOP板块
                3. 资金面分析：
                   - 北向资金流向
                   - 主力资金动向
                   - 融资融券余额变化
                4. 市场情绪判断：
                   - 综合情绪指数评估
                   - 短期趋势预判（偏多/偏空/震荡）
                5. 重点关注方向和建议

                返回标准JSON格式：
                {
                  "analysisDate": "日期",
                  "marketOverview": {
                    "indices": [{"name": "指数名", "value": 点数, "change": "涨跌幅", "volume": "成交量"}],
                    "totalVolume": "总成交额",
                    "advanceDeclineRatio": "涨跌比"
                  },
                  "sectorAnalysis": {
                    "topGainers": [{"sector": "板块名", "change": "涨幅"}],
                    "topLosers": [{"sector": "板块名", "change": "跌幅"}],
                    "rotationTrend": "板块轮动趋势描述"
                  },
                  "capitalFlow": {
                    "northBound": "北向资金净额",
                    "mainForce": "主力资金净额",
                    "marginBalance": "融资融券余额"
                  },
                  "sentimentIndex": 0-100,
                  "shortTermOutlook": "偏多/偏空/震荡",
                  "focusAreas": ["关注方向1", "关注方向2"],
                  "riskWarnings": ["风险提示1"]
                }
                """, analysisDate, marketData);
    }

    @Override
    public String getTemplateName() {
        return "MarketAnalysisPrompt";
    }
}
