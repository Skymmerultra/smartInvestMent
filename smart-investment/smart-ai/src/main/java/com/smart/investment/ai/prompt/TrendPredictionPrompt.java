package com.smart.investment.ai.prompt;

import java.util.Map;

/**
 * 趋势预测 Prompt (T-06)
 * <p>
 * 基于历史数据和技术指标进行短期/中期趋势预测。
 * 输出格式：结构化 JSON，包含趋势方向、支撑压力位、技术信号。
 */
public class TrendPredictionPrompt implements PromptTemplate {

    @Override
    public String buildPrompt(Map<String, Object> params) {
        String securityCode = (String) params.getOrDefault("securityCode", "");
        String securityName = (String) params.getOrDefault("securityName", "");
        String historicalData = (String) params.getOrDefault("historicalData", "");
        String predictionPeriod = (String) params.getOrDefault("predictionPeriod", "短期(1-2周)");

        return String.format("""
                请对以下证券进行%s趋势预测分析。

                证券代码：%s
                证券名称：%s

                历史数据（含K线、成交量、技术指标）：
                %s

                分析要求：
                1. 趋势方向判断（上涨/下跌/横盘震荡）
                2. 关键技术位分析：
                   - 支撑位（列出2-3个关键支撑价位及强度）
                   - 压力位（列出2-3个关键压力价位及强度）
                3. 技术指标信号：
                   - MACD 信号
                   - RSI 位置及信号
                   - 均线系统排列（多头/空头/交叉）
                   - 布林带位置
                4. 成交量分析（放量/缩量趋势判断）
                5. 预测置信度（0-100%）

                返回标准JSON格式：
                {
                  "securityCode": "证券代码",
                  "securityName": "证券名称",
                  "predictionPeriod": "预测周期",
                  "trendDirection": "上涨/下跌/震荡",
                  "confidence": 75,
                  "supportLevels": [
                    {"price": 价格, "strength": "强/中/弱"}
                  ],
                  "resistanceLevels": [
                    {"price": 价格, "strength": "强/中/弱"}
                  ],
                  "technicalSignals": {
                    "macd": "金叉/死叉/粘合",
                    "rsi": {"value": 数值, "signal": "超买/超卖/正常"},
                    "maSystem": "多头排列/空头排列/交叉",
                    "bollingerBand": "上轨/中轨/下轨"
                  },
                  "volumeAnalysis": "成交量分析描述",
                  "riskWarnings": ["风险提示"]
                }
                """, predictionPeriod, securityCode, securityName, historicalData);
    }

    @Override
    public String getTemplateName() {
        return "TrendPredictionPrompt";
    }
}
