package com.smart.investment.ai.prompt;

import java.util.Map;

/**
 * 产业链分析 Prompt (T-06)
 * <p>
 * 分析产业链上下游关系及竞争格局。
 * 输出格式：结构化 JSON，包含产业链图谱、关键节点、上下游景气度。
 */
public class ChainAnalysisPrompt implements PromptTemplate {

    @Override
    public String buildPrompt(Map<String, Object> params) {
        String industry = (String) params.getOrDefault("industry", "");
        String relatedCompaniesData = (String) params.getOrDefault("relatedCompaniesData", "");

        return String.format("""
                请对以下行业进行产业链分析。

                行业：%s
                相关公司数据：
                %s

                分析要求：
                1. 绘制产业链结构：
                   - 上游原材料/核心零部件
                   - 中游制造/服务
                   - 下游应用/消费
                2. 各环节关键公司及竞争格局
                3. 产业链利润分配分析
                4. 各环节景气度判断（高景气/稳定/低迷）
                5. 产业链瓶颈和机会点识别
                6. 替代威胁和技术变革趋势

                返回标准JSON格式：
                {
                  "industry": "行业名",
                  "chainStructure": {
                    "upstream": {
                      "description": "上游描述",
                      "prosperity": "高景气/稳定/低迷",
                      "keyCompanies": [{"name": "公司名", "marketShare": "市场份额描述", "competitiveAdvantage": "竞争优势"}],
                      "keyInputs": ["关键原材料/部件1", "关键原材料/部件2"]
                    },
                    "midstream": {
                      "description": "中游描述",
                      "prosperity": "高景气/稳定/低迷",
                      "keyCompanies": [...],
                      "coreTechnologies": ["核心技术1"]
                    },
                    "downstream": {
                      "description": "下游描述",
                      "prosperity": "高景气/稳定/低迷",
                      "keyCompanies": [...],
                      "demandDrivers": ["需求驱动因素1"]
                    }
                  },
                  "profitDistribution": "利润分配分析",
                  "bottlenecks": ["瓶颈点1", "瓶颈点2"],
                  "opportunities": ["机会点1", "机会点2"],
                  "threats": ["威胁1", "威胁2"],
                  "technologyTrends": ["技术趋势1"],
                  "overallAssessment": "产业链整体评估"
                }
                """, industry, relatedCompaniesData);
    }

    @Override
    public String getTemplateName() {
        return "ChainAnalysisPrompt";
    }
}
