package com.smart.investment.module.news.util;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * SimHash 工具类
 * <p>
 * 用于新闻去重：对文本内容计算 SimHash 指纹，
 * 通过汉明距离判断两篇文章是否相似。
 */
public final class SimHashUtils {

    private SimHashUtils() {
        throw new UnsupportedOperationException("工具类不可实例化");
    }

    /** 指纹位数 */
    private static final int HASH_BITS = 64;

    /** 去重阈值：汉明距离 <= 3 视为重复 */
    private static final int DUPLICATE_THRESHOLD = 3;

    /** 中文分词简单模式 */
    private static final Pattern WORD_PATTERN = Pattern.compile("[\\u4e00-\\u9fa5]{2,}");

    /**
     * 计算文本的 SimHash 指纹
     */
    public static long simHash(String text) {
        if (text == null || text.isEmpty()) {
            return 0L;
        }

        List<String> words = tokenize(text);
        if (words.isEmpty()) {
            return 0L;
        }

        int[] vector = new int[HASH_BITS];

        for (String word : words) {
            long hash = fnvHash(word);
            for (int i = 0; i < HASH_BITS; i++) {
                if (((hash >> i) & 1) == 1) {
                    vector[i]++;
                } else {
                    vector[i]--;
                }
            }
        }

        long fingerprint = 0L;
        for (int i = 0; i < HASH_BITS; i++) {
            if (vector[i] > 0) {
                fingerprint |= (1L << i);
            }
        }
        return fingerprint;
    }

    /**
     * 计算两个 SimHash 指纹的汉明距离
     */
    public static int hammingDistance(long hash1, long hash2) {
        long xor = hash1 ^ hash2;
        return Long.bitCount(xor);
    }

    /**
     * 判断是否重复
     */
    public static boolean isDuplicate(long hash1, long hash2) {
        return hammingDistance(hash1, hash2) <= DUPLICATE_THRESHOLD;
    }

    /**
     * 简单中文分词：提取连续中文字符
     */
    private static List<String> tokenize(String text) {
        List<String> words = new ArrayList<>();
        java.util.regex.Matcher matcher = WORD_PATTERN.matcher(text);
        while (matcher.find()) {
            words.add(matcher.group());
        }
        return words;
    }

    /**
     * FNV-1a 哈希
     */
    private static long fnvHash(String str) {
        long hash = 0xcbf29ce484222325L;
        byte[] bytes = str.getBytes(StandardCharsets.UTF_8);
        for (byte b : bytes) {
            hash ^= (b & 0xff);
            hash *= 0x100000001b3L;
        }
        return hash;
    }
}
