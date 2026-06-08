package com.smart.investment.ai.vector;

/**
 * 文本嵌入服务接口 (T-06)
 * <p>
 * 将文本转换为向量表示，用于语义搜索和相似度计算。
 */
public interface EmbeddingService {

    /**
     * 将文本转换为向量
     *
     * @param text 输入文本
     * @return 向量数组
     */
    float[] embed(String text);

    /**
     * 批量文本嵌入
     *
     * @param texts 输入文本列表
     * @return 向量数组列表
     */
    default float[][] embedBatch(java.util.List<String> texts) {
        if (texts == null || texts.isEmpty()) {
            return new float[0][];
        }
        float[][] embeddings = new float[texts.size()][];
        for (int i = 0; i < texts.size(); i++) {
            embeddings[i] = embed(texts.get(i));
        }
        return embeddings;
    }
}
