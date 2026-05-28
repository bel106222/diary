package com.example.diary.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

//Здесь создаём бин BCryptPasswordEncoder и определяем путь загрузки файлов.
@Configuration
public class AppConfig {
    @Value("${file.upload-dir}")
    private String uploadDir;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Возвращает путь к папке для сохранения файлов
    @Bean
    public String uploadDir() {
        return uploadDir;
    }
}