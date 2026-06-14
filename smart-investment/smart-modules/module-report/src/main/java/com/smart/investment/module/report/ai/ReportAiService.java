package com.smart.investment.module.report.ai;

import com.smart.investment.ai.prompt.ReportCrossCheckPrompt;
import com.smart.investment.ai.prompt.ReportExtractPrompt;
import com.smart.investment.ai.prompt.ReportOcrPrompt;
import com.smart.investment.ai.service.AiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 财报 AI 服务 (T-06)
 * <p>
 * 封装对 ReportOcrPrompt、ReportExtractPrompt、ReportCrossCheckPrompt 的调用，
 * 通过 T-06 的 AiService 执行实际 AI 请求。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReportAiService {

    private final AiService aiService;

    /**
     * OCR 识别：对提取的 PDF 文本进行结构化识别
     *
     * @param ocrText    PDF 提取的文本
     * @param reportType 报告类型
     * @return 结构化后的文本
     */
    public String ocr(String ocrText, String reportType) {
        ReportOcrPrompt prompt = new ReportOcrPrompt();
        Map<String, Object> params = Map.of(
                "imageBase64", ocrText,
                "reportType", reportType
        );
        return aiService.callRaw(prompt, params);
    }

    /**
     * 财务指标提取
     *
     * @param ocrText      OCR 识别后的文本
     * @param companyName  公司名称
     * @param reportPeriod 报告期
     * @return 结构化财务指标 JSON
     */
    public String extractIndicators(String ocrText, String companyName, String reportPeriod) {
        ReportExtractPrompt prompt = new ReportExtractPrompt();
        Map<String, Object> params = Map.of(
                "ocrText", ocrText,
                "companyName", companyName,
                "reportPeriod", reportPeriod
        );
        return aiService.callRaw(prompt, params);
    }

    /**
     * 交叉验证
     *
     * @param currentData  当期数据
     * @param previousData 上期数据
     * @param companyName  公司名称
     * @return 交叉验证结果 JSON
     */
    public String crossCheck(String currentData, String previousData, String companyName) {
        ReportCrossCheckPrompt prompt = new ReportCrossCheckPrompt();
        Map<String, Object> params = Map.of(
                "currentPeriodData", currentData,
                "previousPeriodData", previousData,
                "companyName", companyName
        );
        return aiService.callRaw(prompt, params);
    }
}
