package com.smart.investment.common.core.es;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.mapping.TypeMapping;
import co.elastic.clients.elasticsearch.indices.CreateIndexRequest;
import co.elastic.clients.elasticsearch.indices.ExistsRequest;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.StringReader;

/**
 * ES 索引管理工具 (T-08)
 * <p>
 * 应用启动时自动检查索引是否存在，不存在则创建。
 * 定义三个核心索引：财报、新闻、研报。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class IndexManager {

    public static final String INDEX_FIN_REPORT = "idx_fin_report";
    public static final String INDEX_NEWS_ARTICLE = "idx_news_article";
    public static final String INDEX_RESEARCH_REPORT = "idx_research_report";

    private final ElasticsearchClient esClient;

    @PostConstruct
    public void initIndexes() {
        try {
            createIndexIfNotExists(INDEX_FIN_REPORT, getFinReportMapping());
            createIndexIfNotExists(INDEX_NEWS_ARTICLE, getNewsArticleMapping());
            createIndexIfNotExists(INDEX_RESEARCH_REPORT, getResearchReportMapping());
            log.info("ES 索引初始化完成");
        } catch (Exception e) {
            log.error("ES 索引初始化失败，将降级为数据库查询: {}", e.getMessage());
        }
    }

    /**
     * 检查索引是否存在，不存在则创建
     */
    public void createIndexIfNotExists(String indexName, String mappingJson) {
        try {
            boolean exists = esClient.indices().exists(ExistsRequest.of(e -> e.index(indexName))).value();
            if (!exists) {
                TypeMapping mapping = TypeMapping.of(m -> m.withJson(new StringReader(mappingJson)));
                esClient.indices().create(CreateIndexRequest.of(c -> c.index(indexName).mappings(mapping)));
                log.info("ES 索引创建成功: {}", indexName);
            } else {
                log.debug("ES 索引已存在: {}", indexName);
            }
        } catch (Exception e) {
            log.error("创建 ES 索引失败: {}, 原因: {}", indexName, e.getMessage());
        }
    }

    /**
     * 删除索引
     */
    public void deleteIndex(String indexName) {
        try {
            esClient.indices().delete(d -> d.index(indexName));
            log.info("ES 索引已删除: {}", indexName);
        } catch (Exception e) {
            log.error("删除 ES 索引失败: {}, 原因: {}", indexName, e.getMessage());
        }
    }

    // ==================== 索引 Mapping 定义 ====================

    /**
     * 财报全文检索索引 Mapping
     */
    private String getFinReportMapping() {
        return """
        {
          "properties": {
            "company_code": { "type": "keyword" },
            "company_name": { "type": "text", "analyzer": "ik_max_word", "fields": { "raw": { "type": "keyword" } } },
            "report_period": { "type": "keyword" },
            "report_type": { "type": "keyword" },
            "title": { "type": "text", "analyzer": "ik_max_word" },
            "content": { "type": "text", "analyzer": "ik_max_word" },
            "parse_status": { "type": "keyword" },
            "key_metrics": { "type": "text" },
            "file_url": { "type": "keyword", "index": false },
            "created_at": { "type": "date", "format": "yyyy-MM-dd HH:mm:ss" },
            "updated_at": { "type": "date", "format": "yyyy-MM-dd HH:mm:ss" }
          }
        }""";
    }

    /**
     * 新闻全文检索索引 Mapping
     */
    private String getNewsArticleMapping() {
        return """
        {
          "properties": {
            "source": { "type": "keyword" },
            "source_url": { "type": "keyword", "index": false },
            "title": { "type": "text", "analyzer": "ik_max_word" },
            "content": { "type": "text", "analyzer": "ik_max_word" },
            "summary": { "type": "text", "analyzer": "ik_max_word" },
            "sentiment": { "type": "keyword" },
            "sentiment_score": { "type": "double" },
            "related_securities": { "type": "keyword" },
            "related_industries": { "type": "keyword" },
            "tags": { "type": "keyword" },
            "published_at": { "type": "date", "format": "yyyy-MM-dd HH:mm:ss" },
            "crawled_at": { "type": "date", "format": "yyyy-MM-dd HH:mm:ss" }
          }
        }""";
    }

    /**
     * 研报全文检索索引 Mapping
     */
    private String getResearchReportMapping() {
        return """
        {
          "properties": {
            "security_code": { "type": "keyword" },
            "security_name": { "type": "text", "analyzer": "ik_max_word", "fields": { "raw": { "type": "keyword" } } },
            "title": { "type": "text", "analyzer": "ik_max_word" },
            "content": { "type": "text", "analyzer": "ik_max_word" },
            "viewpoint_summary": { "type": "text", "analyzer": "ik_max_word" },
            "rating": { "type": "keyword" },
            "target_price": { "type": "double" },
            "institution": { "type": "text", "analyzer": "ik_max_word", "fields": { "raw": { "type": "keyword" } } },
            "analyst": { "type": "keyword" },
            "report_date": { "type": "date", "format": "yyyy-MM-dd" },
            "created_at": { "type": "date", "format": "yyyy-MM-dd HH:mm:ss" }
          }
        }""";
    }
}
