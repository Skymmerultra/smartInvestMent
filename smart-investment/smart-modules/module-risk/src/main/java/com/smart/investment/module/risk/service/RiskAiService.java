package com.smart.investment.module.risk.service;

import com.smart.investment.ai.prompt.RiskAssessmentPrompt;
import com.smart.investment.ai.service.AiService;
import com.smart.investment.module.risk.dto.RiskAssessmentResultDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class RiskAiService {

    private final AiService aiService;

    public RiskAssessmentResultDTO assessRisk(String targetType, String targetName, String targetData) {
        Map<String, Object> params = new HashMap<>();
        params.put("targetType", targetType);
        params.put("targetName", targetName);
        params.put("targetData", targetData);
        
        try {
            RiskAssessmentPrompt riskAssessmentPrompt = new RiskAssessmentPrompt();
            RiskAssessmentResultDTO result = aiService.call(riskAssessmentPrompt, params, RiskAssessmentResultDTO.class);
            log.info("AI风险评估完成: targetName={}, overallRiskLevel={}", 
                    targetName, result.getOverallRiskLevel());
            return result;
        } catch (Exception e) {
            log.error("AI风险评估失败: {}", e.getMessage());
            throw new RuntimeException("风险评估服务暂时不可用", e);
        }
    }
}