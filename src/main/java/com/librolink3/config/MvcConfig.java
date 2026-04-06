package com.librolink3.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class MvcConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Esto sirve los archivos de uploads/img a la URL /uploads/** 
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/img/");
    }
}