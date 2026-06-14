package com.smart.investment.module.report.es;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import com.smart.investment.common.core.es.BaseSearchService;
import com.smart.investment.common.core.es.IndexManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 财报 ES 搜索服务 (T-08)
 * <p>
 * 继承 BaseSearchService，实现财报全文搜索。
 * 支持高亮搜索 content 和 title 字段。
 */
@Slf4j
@Service
public class ReportSearchService extends BaseSearchService<FinReportDocument> {

    public ReportSearchService(ElasticsearchClient esClient) {
        super(esClient);
    }

    @Override
    protected String getIndexName() {
        return IndexManager.INDEX_FIN_REPORT;
    }

    @Override
    protected Class<FinReportDocument> getDocumentClass() {
        return FinReportDocument.class;
    }

    @Override
    protected List<String> getHighlightFields() {
        return List.of("content", "title");
    }

    /**
     * 将高亮结果注入文档
     */
    @Override
    protected void applyHighlight(FinReportDocument document, Map<String, List<String>> highlights) {
        if (highlights == null || highlights.isEmpty()) {
            return;
        }
        // 将高亮的 content 替换回文档
        List<String> contentHighlights = highlights.getOrDefault("content", Collections.emptyList());
        if (!contentHighlights.isEmpty()) {
            String highlighted = String.join(" ... ", contentHighlights);
            document.setContent(highlighted);
        }
        // 将高亮的 title 替换回文档
        List<String> titleHighlights = highlights.getOrDefault("title", Collections.emptyList());
        if (!titleHighlights.isEmpty()) {
            document.setTitle(titleHighlights.get(0));
        }
    }
}
