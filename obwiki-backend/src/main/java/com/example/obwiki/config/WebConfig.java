package com.example.obwiki.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/avatar/**")
                .addResourceLocations("file:" + System.getProperty("user.dir") + "/avatar/");
        registry.addResourceHandler("/obwiki/avatar/**")
                .addResourceLocations("file:" + System.getProperty("user.dir") + "/avatar/");
    }
} 