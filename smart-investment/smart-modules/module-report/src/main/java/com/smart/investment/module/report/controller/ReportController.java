package com.smart.investment.module.report.controller;

import com.smart.investment.common.core.constant.Constants;
import com.smart.investment.common.core.result.PageResult;
import com.smart.investment.common.core.result.Result;
import com.smart.investment.module.report.dto.ReportDetailResponse;
import com.smart.investment.module.report.dto.ReportIndicatorResponse;
import com.smart.investment.module.report.dto.ReportUploadResponse;
import com.smart.investment.module.report.es.FinReportDocument;
import com.smart.investment.module.report.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 财报分析控制器 (T-12)
 * <p>
 * 提供财报上传、查询、指标明细和全文搜索接口。
 */
@Slf4j
@RestController
@RequestMapping("/api/report")
@RequiredArgsConstructor
@Tag(name = "财报分析", description = "财报上传、OCR 解析、财务指标查询")
public class ReportController {

    private final ReportService reportService;

    /**
     * 上传财报 PDF 文件（需要 ANALYST 角色）
     * <p>
     * 异步提交 OCR 解析任务到 RabbitMQ q.report.ocr。
     */
    @PostMapping("/upload")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @PreAuthorize("hasRole('" + Constants.ROLE_ANALYST + "')")
    @Operation(summary = "上传财报PDF", description = "上传财报PDF文件，异步进行OCR解析。文件大小不超过20MB，仅支持PDF格式。")
    public Result<ReportUploadResponse> upload(
            @Parameter(description = "财报PDF文件") @RequestParam("file") MultipartFile file,
            @Parameter(description = "公司代码") @RequestParam("companyCode") String companyCode,
            @Parameter(description = "公司名称") @RequestParam("companyName") String companyName,
            @Parameter(description = "报告期（如2025Q4）") @RequestParam("reportPeriod") String reportPeriod,
            @Parameter(description = "报告类型（ANNUAL/QUARTER/MONTHLY）") @RequestParam("reportType") String reportType) {

        log.info("收到财报上传请求: companyCode={}, reportPeriod={}", companyCode, reportPeriod);

        ReportUploadResponse response = reportService.uploadReport(
                file, companyCode, companyName, reportPeriod, reportType);
        return Result.success("财报上传成功，正在异步解析中", response);
    }

    /**
     * 查询财报详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "查询财报详情", description = "根据财报ID查询财报分析结果详情")
    public Result<ReportDetailResponse> getById(
            @Parameter(description = "财报ID") @PathVariable("id") Long id) {
        ReportDetailResponse response = reportService.getReportById(id);
        return Result.success(response);
    }

    /**
     * 财报列表（分页，支持按公司代码/报告期筛选）
     */
    @GetMapping("/list")
    @Operation(summary = "财报列表", description = "分页查询财报列表，支持按公司代码和报告期筛选")
    public Result<PageResult<ReportDetailResponse>> list(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "公司代码") @RequestParam(required = false) String companyCode,
            @Parameter(description = "报告期") @RequestParam(required = false) String reportPeriod) {

        if (page < 1) page = Constants.DEFAULT_PAGE;
        if (size < 1 || size > Constants.MAX_PAGE_SIZE) size = Constants.DEFAULT_PAGE_SIZE;

        PageResult<ReportDetailResponse> result = reportService.listReports(page, size, companyCode, reportPeriod);
        return Result.success(result);
    }

    /**
     * 查询财务指标明细
     */
    @GetMapping("/{id}/indicators")
    @Operation(summary = "财务指标明细", description = "查询指定财报提取的财务指标详细数据")
    public Result<List<ReportIndicatorResponse>> getIndicators(
            @Parameter(description = "财报ID") @PathVariable("id") Long id) {
        List<ReportIndicatorResponse> indicators = reportService.getIndicators(id);
        return Result.success(indicators);
    }

    /**
     * ES 全文搜索财报
     */
    @GetMapping("/search")
    @Operation(summary = "全文搜索财报", description = "通过Elasticsearch全文搜索财报内容")
    public Result<PageResult<FinReportDocument>> search(
            @Parameter(description = "搜索关键词") @RequestParam String keyword,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "20") int size) {

        if (page < 1) page = Constants.DEFAULT_PAGE;
        if (size < 1 || size > Constants.MAX_PAGE_SIZE) size = Constants.DEFAULT_PAGE_SIZE;

        PageResult<FinReportDocument> result = reportService.searchReports(keyword, page, size);
        return Result.success(result);
    }
}
