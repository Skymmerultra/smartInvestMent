package com.smart.investment.common.web.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Plus 分页插件配置
 * <p>
 * 注册 PaginationInnerInterceptor，配置合理的分页上限防止全表扫描
 */
@Configuration
public class MyBatisPlusConfig {

    /**
     * 分页拦截器
     * <p>
     * 自动对分页查询应用MySQL分页策略，最大单页500条防止全表扫描
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();

        PaginationInnerInterceptor paginationInnerInterceptor = new PaginationInnerInterceptor(DbType.MYSQL);
        // 设置合理的分页上限，防止全表扫描（单页最大 500 条）
        paginationInnerInterceptor.setMaxLimit(500L);
        // 溢出处理：请求页码超过最大页时回到首页
        paginationInnerInterceptor.setOverflow(true);

        interceptor.addInnerInterceptor(paginationInnerInterceptor);
        return interceptor;
    }
}
