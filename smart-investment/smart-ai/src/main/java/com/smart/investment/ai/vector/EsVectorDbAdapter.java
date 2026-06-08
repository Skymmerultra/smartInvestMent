package com.smart.investment.ai.vector;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.KnnSearch;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Elasticsearch 向量数据库适配器 (T-06)
 * <p>
 * 基于 ES dense_vector 类型实现向量存储和 kNN 检索。
 * 使用 cosine 相似度计算。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EsVectorDbAdapter implements VectorDbAdapter {

    /**
     * ES 向量索引名称
     */
    public static final String INDEX_VECTOR = "idx_vector_store";

    /**
     * 向量维度（DeepSeek embedding 默认维度）
     */
    public static final int VECTOR_DIMENSION = 1536;

    private final ElasticsearchClient esClient;

    @Override
    public void store(String id, float[] embedding, Map<String, Object> metadata) {
        try {
            Map<String, Object> document = new HashMap<>(metadata != null ? metadata : new HashMap<>());
            document.put("embedding", embedding);
            document.put("id", id);
            document.put("timestamp", System.currentTimeMillis());

            esClient.index(IndexRequest.of(i -> i
                    .index(INDEX_VECTOR)
                    .id(id)
                    .document(document)));

            log.debug("向量存储成功: index={}, id={}, dim={}", INDEX_VECTOR, id, embedding.length);
        } catch (Exception e) {
            log.error("向量存储失败: id={}, error={}", id, e.getMessage());
        }
    }

    @Override
    public void batchStore(List<VectorEntry> entries) {
        if (entries == null || entries.isEmpty()) {
            return;
        }
        for (VectorEntry entry : entries) {
            store(entry.id(), entry.embedding(), entry.metadata());
        }
    }

    @Override
    public List<VectorSearchResult> search(float[] embedding, int topK) {
        try {
            // 使用 kNN 查询进行向量相似度搜索
            SearchRequest request = SearchRequest.of(s -> s
                    .index(INDEX_VECTOR)
                    .knn(KnnSearch.of(k -> k
                            .field("embedding")
                            .queryVector(toFloatList(embedding))
                            .k(topK)
                            .numCandidates(topK * 2)))
            );

            SearchResponse<Map> response = esClient.search(request, Map.class);

            return response.hits().hits().stream()
                    .map(hit -> {
                        String id = (String) hit.source().getOrDefault("id", hit.id());
                        float score = hit.score() != null ? hit.score().floatValue() : 0f;
                        @SuppressWarnings("unchecked")
                        Map<String, Object> metadata = (Map<String, Object>) hit.source();
                        // 移除内部的 embedding 字段（太重）
                        metadata.remove("embedding");
                        return new VectorSearchResult(id, score, metadata);
                    })
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("向量搜索失败: error={}", e.getMessage());
            return List.of();
        }
    }

    @Override
    public void delete(String id) {
        try {
            esClient.delete(d -> d.index(INDEX_VECTOR).id(id));
            log.debug("向量删除成功: index={}, id={}", INDEX_VECTOR, id);
        } catch (Exception e) {
            log.error("向量删除失败: id={}, error={}", id, e.getMessage());
        }
    }

    /**
     * float[] 转 List&lt;Float&gt;
     */
    private List<Float> toFloatList(float[] arr) {
        List<Float> list = new java.util.ArrayList<>(arr.length);
        for (float v : arr) {
            list.add(v);
        }
        return list;
    }

    /**
     * 获取向量索引的 Mapping JSON
     */
    public static String getVectorIndexMapping() {
        return """
        {
          "properties": {
            "id": { "type": "keyword" },
            "embedding": {
              "type": "dense_vector",
              "dims": %d,
              "index": true,
              "similarity": "cosine"
            },
            "timestamp": { "type": "long" }
          }
        }""".formatted(VECTOR_DIMENSION);
    }
}
