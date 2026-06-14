package com.smart.investment.module.risk.service;

import com.smart.investment.module.risk.dto.RiskDashboardDTO;
import com.smart.investment.module.risk.entity.RiskRecord;
import com.smart.investment.module.risk.mapper.RiskRecordMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class RiskService {

    private final RiskRecordMapper riskRecordMapper;

    @Transactional
    public RiskRecord calculateAndSaveRisk(Long portfolioId, String riskType, List<BigDecimal> returns) {
        BigDecimal varValue = calculateVaR(returns, 0.95);
        BigDecimal esValue = calculateES(returns, varValue);
        BigDecimal threshold = getDefaultThreshold(riskType);
        
        RiskRecord record = RiskRecord.builder()
                .portfolioId(portfolioId)
                .riskType(riskType)
                .varValue(varValue)
                .esValue(esValue)
                .threshold(threshold)
                .isAlerted(varValue.compareTo(threshold) > 0 ? 1 : 0)
                .createdAt(LocalDateTime.now())
                .build();
        
        riskRecordMapper.insert(record);
        log.info("风险记录已保存: portfolioId={}, riskType={}, var={}, es={}", 
                portfolioId, riskType, varValue, esValue);
        
        return record;
    }

    public BigDecimal calculateVaR(List<BigDecimal> returns, double confidenceLevel) {
        if (returns == null || returns.isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        List<BigDecimal> sortedReturns = new ArrayList<>(returns);
        sortedReturns.sort(Comparator.naturalOrder());
        
        int index = (int) Math.floor((1 - confidenceLevel) * sortedReturns.size());
        if (index < 0) index = 0;
        if (index >= sortedReturns.size()) index = sortedReturns.size() - 1;
        
        return sortedReturns.get(index).abs();
    }

    public BigDecimal calculateES(List<BigDecimal> returns, BigDecimal varValue) {
        if (returns == null || returns.isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        List<BigDecimal> tailLosses = returns.stream()
                .filter(r -> r.abs().compareTo(varValue) >= 0)
                .map(BigDecimal::abs)
                .toList();
        
        if (tailLosses.isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        return tailLosses.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(tailLosses.size()), 4, RoundingMode.HALF_UP);
    }

    public RiskDashboardDTO getDashboard(Long portfolioId) {
        List<RiskRecord> records = riskRecordMapper.selectByPortfolioId(portfolioId);
        
        Map<String, RiskRecord> latestRecords = new HashMap<>();
        for (RiskRecord record : records) {
            String key = record.getRiskType();
            latestRecords.merge(key, record, (existing, newRecord) -> 
                    newRecord.getCreatedAt().isAfter(existing.getCreatedAt()) ? newRecord : existing);
        }
        
        BigDecimal totalVar = BigDecimal.ZERO;
        BigDecimal totalEs = BigDecimal.ZERO;
        int alertCount = 0;
        LocalDateTime lastUpdate = null;
        
        for (RiskRecord record : latestRecords.values()) {
            totalVar = totalVar.add(record.getVarValue());
            totalEs = totalEs.add(record.getEsValue());
            if (record.getIsAlerted() == 1) {
                alertCount++;
            }
            if (lastUpdate == null || record.getCreatedAt().isAfter(lastUpdate)) {
                lastUpdate = record.getCreatedAt();
            }
        }
        
        return RiskDashboardDTO.builder()
                .portfolioId(portfolioId)
                .totalVar(totalVar)
                .totalEs(totalEs)
                .marketRisk(buildRiskOverview(latestRecords.get("MARKET")))
                .creditRisk(buildRiskOverview(latestRecords.get("CREDIT")))
                .liquidityRisk(buildRiskOverview(latestRecords.get("LIQUIDITY")))
                .alertCount(alertCount)
                .lastUpdateTime(lastUpdate)
                .build();
    }

    private RiskDashboardDTO.RiskOverview buildRiskOverview(RiskRecord record) {
        if (record == null) {
            return null;
        }
        
        String status = record.getIsAlerted() == 1 ? "ALERT" : "NORMAL";
        String level = assessRiskLevel(record.getVarValue(), record.getThreshold());
        
        return RiskDashboardDTO.RiskOverview.builder()
                .riskType(record.getRiskType())
                .varValue(record.getVarValue())
                .esValue(record.getEsValue())
                .threshold(record.getThreshold())
                .status(status)
                .level(level)
                .build();
    }

    private String assessRiskLevel(BigDecimal varValue, BigDecimal threshold) {
        double ratio = varValue.divide(threshold, 4, RoundingMode.HALF_UP).doubleValue();
        if (ratio >= 1.5) return "HIGH";
        if (ratio >= 1.0) return "MEDIUM";
        return "LOW";
    }

    private BigDecimal getDefaultThreshold(String riskType) {
        return switch (riskType) {
            case "MARKET" -> new BigDecimal("0.10");
            case "CREDIT" -> new BigDecimal("0.05");
            case "LIQUIDITY" -> new BigDecimal("0.08");
            default -> new BigDecimal("0.10");
        };
    }

    public List<RiskRecord> getRiskHistory(Long portfolioId, Integer limit) {
        return riskRecordMapper.selectRecentByPortfolioId(portfolioId, limit);
    }

    public List<RiskRecord> getRecordsByRiskType(String riskType) {
        return riskRecordMapper.selectByRiskType(riskType);
    }
}