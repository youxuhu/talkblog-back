package com.revy.talkblogback.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * HTTP 客户端配置组件。
 * 为服务层提供可注入的 RestTemplate 实例。
 */
@Configuration
public class HttpClientConfig {

    /**
     * 创建 RestTemplate Bean。
     *
     * @return RestTemplate 实例
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}