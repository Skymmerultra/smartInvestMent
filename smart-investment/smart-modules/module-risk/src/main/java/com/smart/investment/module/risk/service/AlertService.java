package com.smart.investment.module.risk.service;

import com.smart.investment.common.core.exception.BusinessException;
import com.smart.investment.common.core.exception.ErrorCode;
import com.smart.investment.common.core.websocket.WebSocketPushService;
import com.smart.investment.module.risk.dto.AlertConfigDTO;
import com.smart.investment.module.risk.dto.AlertEventDTO;
import com.smart.investment.module.risk.entity.AlertConfig;
import com.smart.investment.module.risk.entity.AlertEvent;
import com.smart.investment.module.risk.mapper.AlertConfigMapper;
import com.smart.investment.module.risk.mapper.AlertEventMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlertService {

    private final AlertConfigMapper alertConfigMapper;
    private final AlertEventMapper alertEventMapper;
    private final WebSocketPushService webSocketPushService;

    public List<AlertConfigDTO> getAlertConfigs(Long userId) {
        List<AlertConfig> configs = alertConfigMapper.selectByUserId(userId);
        return configs.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public AlertConfigDTO getAlertConfig(Long userId, String riskType) {
        AlertConfig config = alertConfigMapper.selectByUserIdAndRiskType(userId, riskType);
        return config != null ? convertToDTO(config) : null;
    }

    @Transactional
    public AlertConfigDTO saveAlertConfig(Long userId, AlertConfigDTO dto) {
        AlertConfig existing = alertConfigMapper.selectByUserIdAndRiskType(userId, dto.getRiskType());
        
        AlertConfig config;
        if (existing != null) {
            config = existing;
            config.setVarThreshold(dto.getVarThreshold());
            config.setEsThreshold(dto.getEsThreshold());
            config.setNotifyMethod(dto.getNotifyMethod());
            config.setIsActive(dto.getIsActive() ? 1 : 0);
            config.setUpdatedAt(LocalDateTime.now());
            alertConfigMapper.updateById(config);
        } else {
            config = AlertConfig.builder()
                    .userId(userId)
                    .riskType(dto.getRiskType())
                    .varThreshold(dto.getVarThreshold())
                    .esThreshold(dto.getEsThreshold())
                    .notifyMethod(dto.getNotifyMethod())
                    .isActive(dto.getIsActive() ? 1 : 0)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            alertConfigMapper.insert(config);
        }
        
        log.info("预警配置已更新: userId={}, riskType={}", userId, dto.getRiskType());
        return convertToDTO(config);
    }

    @Transactional
    public void triggerAlert(Long userId, String riskType, BigDecimal triggerValue, BigDecimal thresholdValue) {
        AlertConfig config = alertConfigMapper.selectByUserIdAndRiskType(userId, riskType);
        if (config == null || config.getIsActive() == 0) {
            return;
        }
        
        String alertLevel = triggerValue.divide(thresholdValue, 4, java.math.RoundingMode.HALF_UP)
                .compareTo(new BigDecimal("1.5")) >= 0 ? "CRITICAL" : "WARNING";
        
        String alertContent = String.format("%s风险预警：触发值 %.4f 超过阈值 %.4f", 
                riskType, triggerValue, thresholdValue);
        
        AlertEvent event = AlertEvent.builder()
                .userId(userId)
                .riskType(riskType)
                .alertLevel(alertLevel)
                .triggerValue(triggerValue)
                .thresholdValue(thresholdValue)
                .alertContent(alertContent)
                .isRead(0)
                .createdAt(LocalDateTime.now())
                .build();
        
        alertEventMapper.insert(event);
        
        if ("WEBSOCKET".equals(config.getNotifyMethod())) {
            webSocketPushService.pushToUser(String.valueOf(userId), "/topic/risk/alerts", event);
        }
        
        log.info("预警事件已触发: userId={}, riskType={}, level={}", userId, riskType, alertLevel);
    }

    public List<AlertEventDTO> getAlertEvents(Long userId) {
        List<AlertEvent> events = alertEventMapper.selectByUserId(userId);
        return events.stream().map(this::convertToEventDTO).collect(Collectors.toList());
    }

    public List<AlertEventDTO> getUnreadAlertEvents(Long userId) {
        List<AlertEvent> events = alertEventMapper.selectUnreadByUserId(userId);
        return events.stream().map(this::convertToEventDTO).collect(Collectors.toList());
    }

    @Transactional
    public void markAlertAsRead(Long userId, Long alertId) {
        AlertEvent event = alertEventMapper.selectById(alertId);
        if (event == null || !event.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "预警事件不存在或无权操作");
        }
        alertEventMapper.updateReadStatus(alertId, 1);
        log.info("预警事件已标记为已读: userId={}, alertId={}", userId, alertId);
    }

    @Transactional
    public void markAllAlertsAsRead(Long userId) {
        alertEventMapper.updateAllReadByUserId(userId);
        log.info("所有预警事件已标记为已读: userId={}", userId);
    }

    private AlertConfigDTO convertToDTO(AlertConfig config) {
        return AlertConfigDTO.builder()
                .id(config.getId())
                .riskType(config.getRiskType())
                .varThreshold(config.getVarThreshold())
                .esThreshold(config.getEsThreshold())
                .notifyMethod(config.getNotifyMethod())
                .isActive(config.getIsActive() == 1)
                .build();
    }

    private AlertEventDTO convertToEventDTO(AlertEvent event) {
        return AlertEventDTO.builder()
                .id(event.getId())
                .riskType(event.getRiskType())
                .alertLevel(event.getAlertLevel())
                .triggerValue(event.getTriggerValue())
                .thresholdValue(event.getThresholdValue())
                .alertContent(event.getAlertContent())
                .isRead(event.getIsRead() == 1)
                .createdAt(event.getCreatedAt())
                .build();
    }
}