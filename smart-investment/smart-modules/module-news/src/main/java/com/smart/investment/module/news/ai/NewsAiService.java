package com.smart.investment.module.news.ai;

import com.smart.investment.ai.prompt.NewsSentimentPrompt;
import com.smart.investment.ai.service.AiService;
import com.smart.investment.common.core.utils.JsonUtils;
import com.smart.investment.module.news.dto.SentimentResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 新闻 AI 服务
 * <p>
 * 封装对 NewsSentimentPrompt 的调用，通过 AiService 执行情感分析。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NewsAiService {

    private final AiService aiService;

    /**
     * 分析新闻情感
     *
     * @param title   新闻标题
     * @param content 新闻内容
     * @return 情感分析结果
     */
    public SentimentResult analyzeSentiment(String title, String content) {
        NewsSentimentPrompt prompt = new NewsSentimentPrompt();
        Map<String, Object> params = Map.of(
                "title", title != null ? title : "",
                "content", content != null ? content : ""
        );

        try {
            SentimentResult result = aiService.call(prompt, params, SentimentResult.class);
            log.debug("情感分析完成: title={}, sentiment={}, score={}",
                    title, result != null ? result.getSentiment() : "null",
                    result != null ? result.getSentimentScore() : "null");
            return result;
        } catch (Exception e) {
            log.error("AI 情感分析失败: title={}, error={}", title, e.getMessage());
            // 降级返回中性
            return SentimentResult.builder()
                    .sentiment("neutral")
                    .sentimentScore(0.5)
                    .impactSummary("AI 分析暂不可用")
                    .build();
        }
    }
}
