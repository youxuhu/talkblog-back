package com.revy.talkblogback.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 人脸向量化服务配置组件。
 * 用于读取 application.yaml 中 face.service.* 配置。
 */
@Data
@Component
@ConfigurationProperties(prefix = "face.service")
public class FaceServiceProperties {

    /**
     * Python 人脸服务基础地址，例如 http://localhost:5000。
     */
    private String url;

    /**
     * 向量提取接口路径，默认 /extract。
     */
    private String extractEndpoint = "/extract";

    /**
     * 健康检查接口路径。
     */
    private String healthEndpoint = "/health";

    /**
     * 调用超时时间（毫秒）。
     */
    private int timeout = 30000;
}