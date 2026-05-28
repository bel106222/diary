package com.example.diary.config;

import com.example.diary.interceptor.LoginInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final LoginInterceptor loginInterceptor;
    private final String uploadDir;

    public WebConfig(LoginInterceptor loginInterceptor,
                     @Value("${file.upload-dir}") String uploadDir) {
        this.loginInterceptor = loginInterceptor;
        this.uploadDir = uploadDir;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginInterceptor)
                .addPathPatterns("/**")                 // все запросы
                .excludePathPatterns("/login", "/register",  // кроме страниц входа/регистрации
                        "/css/**", "/js/**", "/images/**"); // статика
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Делаем загруженные файлы доступными по URL /files/...
        registry.addResourceHandler("/files/**")
                .addResourceLocations("file:" + uploadDir + "/");
    }
}