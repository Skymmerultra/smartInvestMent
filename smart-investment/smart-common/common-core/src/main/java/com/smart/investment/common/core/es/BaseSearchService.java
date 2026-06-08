package com.smart.investment.common.core.es;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.*;
import co.elastic.clients.elasticsearch.core.bulk.BulkOperation;
import co.elastic.clients.elasticsearch.core.search.Highlight;
import co.elastic.clients.elasticsearch.core.search.HighlightField;
import com.smart.investment.common.core.result.PageResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 通用 ES 搜索服务基类 (T-08)
 * <p>
 * 各业务模块在 Service 层继承此类，传入对应的索引名使用搜索能力。
 * ES 不可用时降级到数据库 LIKE 查询（由子类重写实现）。
 *
 * @param <T> 索引文档类型
 */
@Slf4j
@RequiredArgsConstructor
public abstract class BaseSearchService<T> {

    protected final ElasticsearchClient esClient;

    /**
     * 返回当前使用的索引名
     */
    protected abstract String getIndexName();

    /**
     * 返回文档类型 Class
     */
    protected abstract Class<T> getDocumentClass();

    /**
     * 返回需要高亮的字段列表
     */
    protected List<String> getHighlightFields() {
        return Collections.singletonList("content");
    }

    // ==================== 全文搜索 ====================

    /**
     * 全文搜索（分页）
     *
     * @param keyword 搜索关键词
     * @param page    页码（从1开始）
     * @param size    每页条数
     * @return 分页搜索结果
     */
    public PageResult<T> searchPage(String keyword, int page, int size) {
        try {
            int from = (page - 1) * size;
            SearchRequest searchRequest = SearchRequest.of(s -> s
                    .index(getIndexName())
                    .from(from)
                    .size(size)
                    .query(Query.of(q -> q
                            .multiMatch(mm -> mm
                                    .query(keyword)
                                    .fields("*")
                            )))
                    .highlight(buildHighlight())
            );

            SearchResponse<T> response = esClient.search(searchRequest, getDocumentClass());
            long total = response.hits().total() != null ? response.hits().total().value() : 0;

            List<T> records = response.hits().hits().stream()
                    .map(hit -> {
                        T doc = hit.source();
                        // 将高亮结果注入（通过 getSource 可拿到原始对象）
                        applyHighlight(doc, hit.highlight());
                        return doc;
                    })
                    .collect(Collectors.toList());

            return PageResult.of(records, total, (long) page, (long) size);
        } catch (Exception e) {
            log.error("ES 搜索异常，降级为数据库查询: keyword={}, index={}, error={}",
                    keyword, getIndexName(), e.getMessage());
            return fallbackSearch(keyword, page, size);
        }
    }

    // ==================== 文档操作 ====================

    /**
     * 索引单个文档
     *
     * @param id  文档ID
     * @param doc 文档对象
     */
    public void index(String id, T doc) {
        try {
            esClient.index(IndexRequest.of(i -> i
                    .index(getIndexName())
                    .id(id)
                    .document(doc)));
            log.debug("ES 文档索引成功: index={}, id={}", getIndexName(), id);
        } catch (Exception e) {
            log.error("ES 文档索引失败: index={}, id={}, error={}", getIndexName(), id, e.getMessage());
        }
    }

    /**
     * 批量索引文档
     *
     * @param documents 文档映射 (id -> doc)
     */
    public void bulkIndex(Map<String, T> documents) {
        if (documents == null || documents.isEmpty()) {
            return;
        }
        try {
            List<BulkOperation> operations = documents.entrySet().stream()
                    .map(entry -> BulkOperation.of(b -> b
                            .index(idx -> idx
                                    .index(getIndexName())
                                    .id(entry.getKey())
                                    .document(entry.getValue()))))
                    .collect(Collectors.toList());

            esClient.bulk(BulkRequest.of(b -> b.operations(operations)));
            log.debug("ES 批量索引成功: index={}, count={}", getIndexName(), documents.size());
        } catch (Exception e) {
            log.error("ES 批量索引失败: index={}, count={}, error={}",
                    getIndexName(), documents.size(), e.getMessage());
        }
    }

    /**
     * 根据ID删除文档
     */
    public void delete(String id) {
        try {
            esClient.delete(DeleteRequest.of(d -> d
                    .index(getIndexName())
                    .id(id)));
            log.debug("ES 文档删除成功: index={}, id={}", getIndexName(), id);
        } catch (Exception e) {
            log.error("ES 文档删除失败: index={}, id={}, error={}", getIndexName(), id, e.getMessage());
        }
    }

    // ==================== 高亮 ====================

    private Highlight buildHighlight() {
        Highlight.Builder builder = new Highlight.Builder();
        for (String field : getHighlightFields()) {
            builder.fields(field, HighlightField.of(hf -> hf
                    .preTags("<em>")
                    .postTags("</em>")
                    .fragmentSize(150)
                    .numberOfFragments(3)));
        }
        return builder.build();
    }

    /**
     * 将高亮结果注入文档（子类可重写以具体处理）
     */
    protected void applyHighlight(T document, Map<String, List<String>> highlights) {
        // 默认空实现，子类可重写
    }

    // ==================== 降级方案 ====================

    /**
     * ES 不可用时的降级搜索（子类重写为数据库 LIKE 查询）
     */
    protected PageResult<T> fallbackSearch(String keyword, int page, int size) {
        log.warn("ES 降级搜索未实现，返回空结果: index={}", getIndexName());
        return PageResult.empty((long) page, (long) size);
    }
}
