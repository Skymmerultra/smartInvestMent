package com.smart.investment.common.web.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 请求日志拦截器
 * <p>
 * 记录每个请求的 method、URI、耗时、响应状态码，使用 SLF4J 输出结构化日志
 */
@Slf4j
@Component
public class RequestLogInterceptor implements HandlerInterceptor {

    private static final String START_TIME_ATTR = "requestStartTime";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        request.setAttribute(START_TIME_ATTR, System.currentTimeMillis());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        Long startTime = (Long) request.getAttribute(START_TIME_ATTR);
        if (startTime == null) {
            return;
        }

        long elapsed = System.currentTimeMillis() - startTime;
        int status = response.getStatus();
        String method = request.getMethod();
        String uri = request.getRequestURI();
        String queryString = request.getQueryString();

        String fullUri = queryString != null ? uri + "?" + queryString : uri;

        if (ex != null) {
            log.warn("请求异常 - method: {}, uri: {}, status: {}, elapsed: {}ms, error: {}",
                    method, fullUri, status, elapsed, ex.getMessage());
        } else {
            log.info("请求完成 - method: {}, uri: {}, status: {}, elapsed: {}ms",
                    method, fullUri, status, elapsed);
        }
    }
}
