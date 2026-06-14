package com.smart.investment;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 智能投研系统 - 启动类
 */
@SpringBootApplication
@EnableScheduling
public class SmartInvestmentApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartInvestmentApplication.class, args);
    }
}
