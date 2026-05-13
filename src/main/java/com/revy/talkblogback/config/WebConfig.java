package com.revy.talkblogback.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    @Value("${file.comment-images-dir:comment-images}")
    private String commentImagesDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String commentImagesPath = "file:" + uploadDir + "/" + commentImagesDir + "/";
        registry.addResourceHandler("/" + uploadDir + "/" + commentImagesDir + "/**")
                .addResourceLocations(commentImagesPath);
    }
}
