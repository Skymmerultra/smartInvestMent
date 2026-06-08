package com.smart.investment.ai.vector;

import java.util.List;
import java.util.Map;

/**
 * 向量数据库适配接口 (T-06)
 * <p>
 * 定义向量存储和相似度检索的标准接口，便于替换底层实现。
 */
public interface VectorDbAdapter {

    /**
     * 存储向量和关联元数据
     *
     * @param id        文档唯一标识
     * @param embedding 向量值
     * @param metadata  关联元数据
     */
    void store(String id, float[] embedding, Map<String, Object> metadata);

    /**
     * 批量存储向量
     *
     * @param entries 批量向量条目
     */
    void batchStore(List<VectorEntry> entries);

    /**
     * 向量相似度搜索
     *
     * @param embedding 查询向量
     * @param topK      返回TopK个最相似结果
     * @return 搜索结果列表（按相似度降序）
     */
    List<VectorSearchResult> search(float[] embedding, int topK);

    /**
     * 根据ID删除向量
     *
     * @param id 文档ID
     */
    void delete(String id);

    /**
     * 向量条目
     */
    record VectorEntry(String id, float[] embedding, Map<String, Object> metadata) {}

    /**
     * 向量搜索结果
     */
    record VectorSearchResult(String id, float score, Map<String, Object> metadata) {}
}
