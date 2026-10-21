package com.srs;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

/**
 * 智慧安防小区运营平台 - 启动类(一期模块化单体)
 */
@EnableAsync
@EnableCaching
@EnableScheduling
@EnableMethodSecurity
@MapperScan("com.srs.**.mapper")
@SpringBootApplication
public class SrsApplication {

    public static void main(String[] args) {
        SpringApplication.run(SrsApplication.class, args);
    }
}
