package com.smart.investment.module.report.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smart.investment.common.core.config.MqDeclareConfig;
import com.smart.investment.common.core.constant.Constants;
import com.smart.investment.common.core.exception.BusinessException;
import com.smart.investment.common.core.exception.ErrorCode;
import com.smart.investment.common.core.mq.BaseMessage;
import com.smart.investment.common.core.result.PageResult;
import com.smart.investment.common.core.utils.JsonUtils;
import com.smart.investment.module.report.ai.ReportAiService;
import com.smart.investment.module.report.dto.*;
import com.smart.investment.module.report.entity.FinIndicator;
import com.smart.investment.module.report.entity.FinNonFinInfo;
import com.smart.investment.module.report.entity.FinReport;
import com.smart.investment.module.report.es.FinReportDocument;
import com.smart.investment.module.report.es.ReportSearchService;
import com.smart.investment.module.report.mapper.FinIndicatorMapper;
import com.smart.investment.module.report.mapper.FinNonFinInfoMapper;
import com.smart.investment.module.report.mapper.FinReportMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 财报服务 (T-12)
 * <p>
 * 负责财报文件上传、异步 OCR 解析、财务指标提取、
 * 非财务信息识别、交叉验证、ES 索引。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReportService {

    private final FinReportMapper reportMapper;
    private final FinIndicatorMapper indicatorMapper;
    private final FinNonFinInfoMapper nonFinInfoMapper;
    private final ReportAiService reportAiService;
    private final RabbitTemplate rabbitTemplate;
    private final ReportSearchService reportSearchService;

    /** 文件上传目录 */
    @Value("${report.upload.dir:${user.dir}/data/uploads/reports}")
    private String uploadDir;

    // ==================== 上传 ====================

    /**
     * 上传财报 PDF 文件
     */
    @Transactional(rollbackFor = Exception.class)
    public ReportUploadResponse uploadReport(MultipartFile file, String companyCode, String companyName,
                                              String reportPeriod, String reportType) {
        if (file.isEmpty() || !Constants.ALLOWED_REPORT_FILE_TYPE.equals(file.getContentType())) {
            throw new BusinessException(ErrorCode.PARAM_FORMAT_ERROR, "仅支持 PDF 格式文件");
        }
        if (file.getSize() > Constants.MAX_FILE_UPLOAD_SIZE) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "文件大小不能超过 20MB");
        }

        String fileUrl = saveFile(file);

        FinReport report = FinReport.builder()
                .companyCode(companyCode)
                .companyName(companyName)
                .reportType(reportType)
                .reportPeriod(reportPeriod)
                .fileUrl(fileUrl)
                .parseStatus(FinReport.STATUS_PENDING)
                .createdAt(LocalDateTime.now())
                .build();
        reportMapper.insert(report);

        final Long reportId = report.getId();
        final ReportOcrMessage ocrMessage = ReportOcrMessage.builder()
                .reportId(reportId)
                .fileUrl(fileUrl)
                .companyCode(companyCode)
                .companyName(companyName)
                .reportPeriod(reportPeriod)
                .build();
        // 延迟到事务提交后再发送 MQ 消息，避免消费者在 DB 记录不可见时消费
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                BaseMessage<ReportOcrMessage> message = BaseMessage.of("REPORT_OCR", ocrMessage);
                rabbitTemplate.convertAndSend(MqDeclareConfig.EXCHANGE_REPORT, MqDeclareConfig.RK_REPORT_OCR, message);
                log.info("OCR 消息已发送（事务已提交）: reportId={}", reportId);
            }
        });

        return ReportUploadResponse.builder()
                .id(report.getId())
                .fileUrl(fileUrl)
                .parseStatus(FinReport.STATUS_PENDING)
                .createdAt(report.getCreatedAt())
                .build();
    }

    // ==================== OCR 异步处理 ====================

    /**
     * 执行 OCR 解析流程（由 MQ 消费者 ReportOcrConsumer 调用）
     * <p>
     * 流程：PENDING → (OCR→提取指标→保存指标→非财务信息→交叉验证→ES索引) → COMPLETED/FAILED
     */
    @Transactional(rollbackFor = Exception.class)
    public void processOcr(ReportOcrMessage ocrMessage) {
        Long reportId = ocrMessage.getReportId();
        log.info("开始 OCR 解析: reportId={}", reportId);

        FinReport report = reportMapper.selectById(reportId);
        if (report == null) {
            log.error("财报不存在: reportId={}", reportId);
            throw new BusinessException(ErrorCode.NOT_FOUND, "财报尚未入库，稍后重试: reportId=" + reportId);
        }

        String companyName = ocrMessage.getCompanyName();
        String title = companyName + "_" + ocrMessage.getCompanyCode() + "_" + report.getReportPeriod();

        try {
            // 1. PDFBox 提取 PDF 文本
            String pdfText = extractPdfText(report.getFileUrl());

            // 2. OCR Prompt 结构化文本
            String ocrContent = reportAiService.ocr(pdfText, report.getReportType());

            // 3. 提取 Prompt → 财务指标
            String extractResult = reportAiService.extractIndicators(
                    ocrContent, companyName, report.getReportPeriod());
            parseAndSaveIndicators(reportId, extractResult);

            // 4. 非财务信息
            parseAndSaveNonFinInfo(reportId, ocrContent);

            // 5. 交叉验证
            String crossCheckResult = performCrossCheck(report, extractResult, companyName);

            // 6. 更新状态为 COMPLETED
            report.setParseStatus(FinReport.STATUS_COMPLETED);
            report.setUpdatedAt(LocalDateTime.now());
            reportMapper.updateById(report);

            // 7. ES 索引
            indexToEs(report, companyName, title, ocrContent, crossCheckResult);

            log.info("OCR 解析完成: reportId={}", reportId);
        } catch (Exception e) {
            log.error("OCR 解析失败: reportId={}, error={}", reportId, e.getMessage(), e);
            report.setParseStatus(FinReport.STATUS_FAILED);
            report.setErrorMsg(e.getMessage().length() > 500 ? e.getMessage().substring(0, 500) : e.getMessage());
            report.setUpdatedAt(LocalDateTime.now());
            reportMapper.updateById(report);

            try {
                indexToEs(report, companyName, title, null, null);
            } catch (Exception ex) {
                log.error("ES 索引失败（已忽略）: reportId={}", reportId, ex);
            }
            throw new RuntimeException("OCR 解析失败: reportId=" + reportId, e);
        }
    }

    // ==================== 查询 ====================

    /**
     * 查询财报详情
     */
    public ReportDetailResponse getReportById(Long id) {
        FinReport report = reportMapper.selectById(id);
        if (report == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "财报不存在");
        }
        return toDetailResponse(report);
    }

    /**
     * 分页查询财报列表（支持按公司代码和报告期筛选）
     */
    public PageResult<ReportDetailResponse> listReports(int page, int size, String companyCode, String reportPeriod) {
        LambdaQueryWrapper<FinReport> wrapper = new LambdaQueryWrapper<FinReport>()
                .eq(companyCode != null && !companyCode.isEmpty(), FinReport::getCompanyCode, companyCode)
                .eq(reportPeriod != null && !reportPeriod.isEmpty(), FinReport::getReportPeriod, reportPeriod)
                .orderByDesc(FinReport::getCreatedAt);

        IPage<FinReport> iPage = reportMapper.selectPage(new Page<>(page, size), wrapper);
        List<ReportDetailResponse> records = iPage.getRecords().stream()
                .map(this::toDetailResponse)
                .collect(Collectors.toList());

        return PageResult.of(records, iPage.getTotal(), iPage.getCurrent(), iPage.getSize());
    }

    /**
     * 查询财务指标明细
     */
    public List<ReportIndicatorResponse> getIndicators(Long reportId) {
        FinReport report = reportMapper.selectById(reportId);
        if (report == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "财报不存在");
        }

        LambdaQueryWrapper<FinIndicator> wrapper = new LambdaQueryWrapper<FinIndicator>()
                .eq(FinIndicator::getReportId, reportId);
        List<FinIndicator> indicators = indicatorMapper.selectList(wrapper);

        return indicators.stream()
                .map(i -> ReportIndicatorResponse.builder()
                        .id(i.getId())
                        .reportId(i.getReportId())
                        .indicatorName(i.getIndicatorName())
                        .indicatorValue(i.getIndicatorValue())
                        .unit(i.getUnit())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * ES 全文搜索财报
     */
    public PageResult<FinReportDocument> searchReports(String keyword, int page, int size) {
        return reportSearchService.searchPage(keyword, page, size);
    }

    // ==================== 私有方法 ====================

    private String saveFile(MultipartFile file) {
        try {
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path filePath = uploadPath.resolve(filename);
            file.transferTo(filePath.toFile());
            log.info("文件保存成功: {}", filePath.toAbsolutePath());
            return filePath.toAbsolutePath().toString();
        } catch (IOException e) {
            log.error("文件保存失败: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.FILE_OPERATION_ERROR, "文件存储失败");
        }
    }

    private String extractPdfText(String filePath) {
        try (PDDocument document = Loader.loadPDF(new File(filePath))) {
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);
            String text = stripper.getText(document);
            log.info("PDF 文本提取成功: length={}", text.length());
            return text;
        } catch (IOException e) {
            log.error("PDF 文本提取失败: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.FILE_OPERATION_ERROR, "PDF 文件读取失败: " + e.getMessage());
        }
    }

    private void parseAndSaveIndicators(Long reportId, String extractResult) {
        try {
            Map<String, Object> resultMap = JsonUtils.parseMap(extractResult);
            @SuppressWarnings("unchecked")
            Map<String, Object> metrics = (Map<String, Object>) resultMap.getOrDefault("metrics", Collections.emptyMap());

            if (metrics.isEmpty()) {
                log.warn("未提取到财务指标: reportId={}", reportId);
                return;
            }

            LocalDateTime now = LocalDateTime.now();
            for (Map.Entry<String, Object> entry : metrics.entrySet()) {
                @SuppressWarnings("unchecked")
                Map<String, Object> metricData = (Map<String, Object>) entry.getValue();
                if (metricData == null) continue;

                String indicatorName = mapIndicatorName(entry.getKey());
                BigDecimal value = toBigDecimal(metricData.get("value"));
                String unit = (String) metricData.getOrDefault("unit", "");

                if (value != null) {
                    FinIndicator indicator = FinIndicator.builder()
                            .reportId(reportId)
                            .indicatorName(indicatorName)
                            .indicatorValue(value)
                            .unit(unit)
                            .createdAt(now)
                            .build();
                    indicatorMapper.insert(indicator);
                }
            }
            log.info("财务指标保存成功: reportId={}, count={}", reportId, metrics.size());
        } catch (Exception e) {
            log.error("财务指标解析保存失败: reportId={}, error={}", reportId, e.getMessage(), e);
        }
    }

    private void parseAndSaveNonFinInfo(Long reportId, String ocrResult) {
        try {
            List<FinNonFinInfo> nonFinInfoList = new ArrayList<>();
            LocalDateTime now = LocalDateTime.now();

            String strategySection = extractSection(ocrResult, "业务战略", "经营战略", "发展战略", "战略规划");
            if (strategySection != null && !strategySection.isEmpty()) {
                nonFinInfoList.add(FinNonFinInfo.builder()
                        .reportId(reportId)
                        .infoType("BUSINESS_STRATEGY")
                        .infoContent(strategySection)
                        .createdAt(now)
                        .build());
            }

            String managementSection = extractSection(ocrResult, "管理层变动", "董事", "监事", "高管变动", "管理层");
            if (managementSection != null && !managementSection.isEmpty()) {
                nonFinInfoList.add(FinNonFinInfo.builder()
                        .reportId(reportId)
                        .infoType("MANAGEMENT_CHANGE")
                        .infoContent(managementSection)
                        .createdAt(now)
                        .build());
            }

            if (nonFinInfoList.isEmpty()) {
                nonFinInfoList.add(FinNonFinInfo.builder()
                        .reportId(reportId)
                        .infoType("SUMMARY")
                        .infoContent(ocrResult.length() > 1000 ? ocrResult.substring(0, 1000) + "..." : ocrResult)
                        .createdAt(now)
                        .build());
            }

            for (FinNonFinInfo info : nonFinInfoList) {
                nonFinInfoMapper.insert(info);
            }
            log.info("非财务信息保存成功: reportId={}, count={}", reportId, nonFinInfoList.size());
        } catch (Exception e) {
            log.error("非财务信息保存失败: reportId={}, error={}", reportId, e.getMessage(), e);
        }
    }

    private String performCrossCheck(FinReport currentReport, String currentData, String companyName) {
        try {
            LambdaQueryWrapper<FinReport> wrapper = new LambdaQueryWrapper<FinReport>()
                    .eq(FinReport::getCompanyCode, currentReport.getCompanyCode())
                    .ne(FinReport::getId, currentReport.getId())
                    .eq(FinReport::getParseStatus, FinReport.STATUS_COMPLETED)
                    .orderByDesc(FinReport::getReportPeriod)
                    .last("LIMIT 1");

            FinReport previousReport = reportMapper.selectOne(wrapper);
            if (previousReport == null) {
                log.info("无上期财报数据，跳过交叉验证: reportId={}", currentReport.getId());
                return null;
            }

            String previousContent = "上期财报ID: " + previousReport.getId()
                    + ", 报告期: " + previousReport.getReportPeriod();
            return reportAiService.crossCheck(currentData, previousContent, companyName);
        } catch (Exception e) {
            log.error("交叉验证失败: reportId={}, error={}", currentReport.getId(), e.getMessage(), e);
            return null;
        }
    }

    private void indexToEs(FinReport report, String companyName, String title,
                           String content, String crossCheckResult) {
        try {
            LambdaQueryWrapper<FinIndicator> wrapper = new LambdaQueryWrapper<FinIndicator>()
                    .eq(FinIndicator::getReportId, report.getId());
            List<FinIndicator> indicators = indicatorMapper.selectList(wrapper);
            String keyMetrics = JsonUtils.toJsonString(indicators.stream()
                    .map(i -> {
                        Map<String, Object> m = new LinkedHashMap<>();
                        m.put("name", i.getIndicatorName());
                        m.put("value", i.getIndicatorValue());
                        m.put("unit", i.getUnit());
                        return m;
                    })
                    .collect(Collectors.toList()));

            String fullContent = (content != null ? content : "");
            if (crossCheckResult != null && !crossCheckResult.isEmpty()) {
                fullContent += "\n\n【交叉验证结果】\n" + crossCheckResult;
            }

            FinReportDocument doc = FinReportDocument.builder()
                    .id(report.getId())
                    .companyCode(report.getCompanyCode())
                    .companyName(companyName)
                    .reportPeriod(report.getReportPeriod())
                    .reportType(report.getReportType())
                    .title(title)
                    .content(fullContent)
                    .parseStatus(report.getParseStatus())
                    .keyMetrics(keyMetrics)
                    .fileUrl(report.getFileUrl())
                    .createdAt(report.getCreatedAt())
                    .updatedAt(report.getUpdatedAt())
                    .build();
            reportSearchService.index(String.valueOf(report.getId()), doc);
            log.info("ES 索引完成: reportId={}", report.getId());
        } catch (Exception e) {
            log.error("ES 索引失败: reportId={}, error={}", report.getId(), e.getMessage(), e);
        }
    }

    private ReportDetailResponse toDetailResponse(FinReport report) {
        return ReportDetailResponse.builder()
                .id(report.getId())
                .companyCode(report.getCompanyCode())
                .companyName(report.getCompanyName())
                .reportType(report.getReportType())
                .reportPeriod(report.getReportPeriod())
                .fileUrl(report.getFileUrl())
                .parseStatus(report.getParseStatus())
                .errorMsg(report.getErrorMsg())
                .createdAt(report.getCreatedAt())
                .updatedAt(report.getUpdatedAt())
                .build();
    }

    private String mapIndicatorName(String key) {
        return switch (key.toLowerCase()) {
            case "revenue" -> "营业收入";
            case "costofrevenue", "cost" -> "营业成本";
            case "grossprofit" -> "毛利润";
            case "grossmargin" -> "毛利率";
            case "netprofit" -> "净利润";
            case "netmargin" -> "净利率";
            case "totalassets" -> "总资产";
            case "totalliabilities" -> "总负债";
            case "debtratio", "assetliabilityratio" -> "资产负债率";
            case "operatingcashflow" -> "经营活动现金流净额";
            case "eps" -> "每股收益";
            case "roe" -> "净资产收益率";
            case "receivablesturnover" -> "应收账款周转率";
            case "inventoryturnover" -> "存货周转率";
            case "rdexpenseratio" -> "研发费用占比";
            default -> key;
        };
    }

    private BigDecimal toBigDecimal(Object value) {
        if (value == null) return null;
        try {
            if (value instanceof Number num) {
                return BigDecimal.valueOf(num.doubleValue());
            }
            return new BigDecimal(value.toString());
        } catch (Exception e) {
            log.warn("数值转换失败: value={}", value);
            return null;
        }
    }

    private String extractSection(String text, String... keywords) {
        if (text == null || text.isEmpty()) return null;
        String[] lines = text.split("\n");
        boolean found = false;
        int foundIndex = -1;

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.isEmpty()) continue;
            for (String keyword : keywords) {
                if (line.contains(keyword)) {
                    found = true;
                    foundIndex = i;
                    break;
                }
            }
            if (found) break;
        }

        if (found && foundIndex >= 0) {
            int end = Math.min(foundIndex + 20, lines.length);
            StringBuilder section = new StringBuilder();
            for (int i = foundIndex; i < end; i++) {
                section.append(lines[i]).append("\n");
            }
            return section.toString().trim();
        }
        return null;
    }
}
