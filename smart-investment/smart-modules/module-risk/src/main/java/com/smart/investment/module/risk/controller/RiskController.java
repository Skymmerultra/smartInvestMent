package com.smart.investment.module.risk.controller;

import com.smart.investment.common.core.result.Result;
import com.smart.investment.module.risk.dto.AlertConfigDTO;
import com.smart.investment.module.risk.dto.AlertEventDTO;
import com.smart.investment.module.risk.dto.RiskAssessmentResultDTO;
import com.smart.investment.module.risk.dto.RiskDashboardDTO;
import com.smart.investment.module.risk.entity.RiskRecord;
import com.smart.investment.module.risk.service.AlertService;
import com.smart.investment.module.risk.service.RiskAiService;
import com.smart.investment.module.risk.service.RiskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/risk")
@RequiredArgsConstructor
public class RiskController {

    private final RiskService riskService;
    private final AlertService alertService;
    private final RiskAiService riskAiService;

    @GetMapping("/dashboard")
    public Result<RiskDashboardDTO> getDashboard(@RequestParam Long portfolioId) {
        RiskDashboardDTO dashboard = riskService.getDashboard(portfolioId);
        return Result.success(dashboard);
    }

    @GetMapping("/records")
    @PreAuthorize("hasRole('ANALYST')")
    public Result<List<RiskRecord>> getRiskRecords(
            @RequestParam Long portfolioId,
            @RequestParam(defaultValue = "20") Integer limit) {
        List<RiskRecord> records = riskService.getRiskHistory(portfolioId, limit);
        return Result.success(records);
    }

    @GetMapping("/alert-config")
    public Result<List<AlertConfigDTO>> getAlertConfigs() {
        Long userId = getCurrentUserId();
        List<AlertConfigDTO> configs = alertService.getAlertConfigs(userId);
        return Result.success(configs);
    }

    @PutMapping("/alert-config")
    @PreAuthorize("hasRole('ANALYST')")
    public Result<AlertConfigDTO> updateAlertConfig(@RequestBody AlertConfigDTO dto) {
        Long userId = getCurrentUserId();
        AlertConfigDTO result = alertService.saveAlertConfig(userId, dto);
        return Result.success("配置更新成功", result);
    }

    @GetMapping("/alerts")
    public Result<List<AlertEventDTO>> getAlertEvents() {
        Long userId = getCurrentUserId();
        List<AlertEventDTO> events = alertService.getAlertEvents(userId);
        return Result.success(events);
    }

    @GetMapping("/alerts/unread")
    public Result<List<AlertEventDTO>> getUnreadAlerts() {
        Long userId = getCurrentUserId();
        List<AlertEventDTO> events = alertService.getUnreadAlertEvents(userId);
        return Result.success(events);
    }

    @PutMapping("/alerts/{id}/read")
    public Result<Void> markAlertAsRead(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        alertService.markAlertAsRead(userId, id);
        return Result.success();
    }

    @PutMapping("/alerts/read-all")
    public Result<Void> markAllAlertsAsRead() {
        Long userId = getCurrentUserId();
        alertService.markAllAlertsAsRead(userId);
        return Result.success();
    }

    @PostMapping("/assessment")
    @PreAuthorize("hasRole('ANALYST')")
    public Result<RiskAssessmentResultDTO> assessRisk(
            @RequestParam String targetType,
            @RequestParam String targetName,
            @RequestBody String targetData) {
        RiskAssessmentResultDTO result = riskAiService.assessRisk(targetType, targetName, targetData);
        return Result.success(result);
    }

    //TODO 写死userId
    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//        return Long.parseLong(auth.getName());
        return 1L;
    }
}