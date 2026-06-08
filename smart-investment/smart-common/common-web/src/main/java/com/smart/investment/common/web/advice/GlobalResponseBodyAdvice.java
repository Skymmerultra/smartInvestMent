package com.smart.investment.common.web.advice;

import com.smart.investment.common.core.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * 全局响应包装切面
 * <p>
 * 对所有 Controller 返回值自动包装为 Result&lt;T&gt; 格式
 * 排除已包装的 Result 类型和 Swagger 资源路径
 */
@Slf4j
@RestControllerAdvice(basePackages = "com.smart.investment")
public class GlobalResponseBodyAdvice implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(MethodParameter returnType,
                            Class<? extends HttpMessageConverter<?>> converterType) {
        // 如果返回值已经是 Result 类型，则不再包装
        if (Result.class.isAssignableFrom(returnType.getParameterType())) {
            return false;
        }
        // 排除 SpringDoc / Swagger 相关
        String packageName = returnType.getDeclaringClass().getPackageName();
        if (packageName.contains("springdoc") || packageName.contains("swagger")) {
            return false;
        }
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body,
                                  MethodParameter returnType,
                                  MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request,
                                  ServerHttpResponse response) {
        // 如果返回值已经是 Result 类型（双重检查），直接返回
        if (body instanceof Result) {
            return body;
        }
        // 如果返回类型是 String，需要特殊处理（Spring MVC 中 String 由 StringHttpMessageConverter 处理）
        if (body instanceof String) {
            return body;
        }
        // 自动包装为 Result 成功响应
        return Result.success(body);
    }
}
