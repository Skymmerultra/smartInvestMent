package com.smart.investment.common.web.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Knife4j / SpringDoc OpenAPI 3 配置
 * <p>
 * 配置 API 分组（按模块分组：认证、财报、行情、产业链、趋势、风险、新闻）
 * 配置全局鉴权头（Authorization: Bearer xxx）、文档信息（标题、版本、描述）
 */
@Configuration
public class Knife4jConfig {

    private static final String SECURITY_SCHEME_NAME = "Authorization";

    /**
     * OpenAPI 基本信息
     */
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("智能投研系统 API 文档")
                        .version("1.0.0")
                        .description("智能投研系统后端接口文档，提供财报分析、行情数据、产业链图谱、趋势预测、风险评估、新闻资讯等接口")
                        .contact(new Contact()
                                .name("Smart Investment Team")
                                .email("dev@smart-investment.com")))
                // 全局鉴权头配置
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME,
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.APIKEY)
                                        .in(SecurityScheme.In.HEADER)
                                        .name("Authorization")
                                        .description("填入 Bearer + 空格 + token，例如：Bearer eyJ...")))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME));
    }

    // ==================== API 分组（按业务模块） ====================

    /**
     * 认证模块 - module-auth
     */
    @Bean
    public GroupedOpenApi authGroup() {
        return GroupedOpenApi.builder()
                .group("认证")
                .packagesToScan("com.smart.investment.module.auth")
                .build();
    }

    /**
     * 财报模块 - module-report
     */
    @Bean
    public GroupedOpenApi reportGroup() {
        return GroupedOpenApi.builder()
                .group("财报")
                .packagesToScan("com.smart.investment.module.report")
                .build();
    }

    /**
     * 行情模块 - module-market
     */
    @Bean
    public GroupedOpenApi marketGroup() {
        return GroupedOpenApi.builder()
                .group("行情")
                .packagesToScan("com.smart.investment.module.market")
                .build();
    }

    /**
     * 产业链模块 - module-chain
     */
    @Bean
    public GroupedOpenApi chainGroup() {
        return GroupedOpenApi.builder()
                .group("产业链")
                .packagesToScan("com.smart.investment.module.chain")
                .build();
    }

    /**
     * 趋势模块 - module-trend
     */
    @Bean
    public GroupedOpenApi trendGroup() {
        return GroupedOpenApi.builder()
                .group("趋势")
                .packagesToScan("com.smart.investment.module.trend")
                .build();
    }

    /**
     * 风险模块 - module-risk
     */
    @Bean
    public GroupedOpenApi riskGroup() {
        return GroupedOpenApi.builder()
                .group("风险")
                .packagesToScan("com.smart.investment.module.risk")
                .build();
    }

    /**
     * 新闻模块 - module-news
     */
    @Bean
    public GroupedOpenApi newsGroup() {
        return GroupedOpenApi.builder()
                .group("新闻")
                .packagesToScan("com.smart.investment.module.news")
                .build();
    }
}
