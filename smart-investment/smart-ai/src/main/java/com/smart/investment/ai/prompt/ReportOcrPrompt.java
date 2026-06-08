package com.smart.investment.ai.prompt;

import java.util.Map;

/**
 * 财报 OCR 识别 Prompt (T-06)
 * <p>
 * 对财报扫描件/图片进行 OCR 文本识别，提取结构化内容。
 */
public class ReportOcrPrompt implements PromptTemplate {

    @Override
    public String buildPrompt(Map<String, Object> params) {
        String imageBase64 = (String) params.getOrDefault("imageBase64", "");
        String reportType = (String) params.getOrDefault("reportType", "年报");

        return String.format("""
                请对以下%s的扫描件进行OCR识别，提取所有文本内容。
                要求：
                1. 保留原始表格结构和数据
                2. 识别所有数字和中文文本
                3. 按章节归类输出（如：资产负债表、利润表、现金流量表、附注等）
                4. 返回结构化的JSON格式数据

                图片Base64数据：
                %s
                """, reportType, imageBase64);
    }

    @Override
    public String getTemplateName() {
        return "ReportOcrPrompt";
    }
}
