package ru.tinkoff.edu.java.bot.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Properties {

    @Bean
    public String botToken(ApplicationConfig config) {
        return config.bot().token();
    }

    @Bean
    public String botUsername(ApplicationConfig config) {
        return config.bot().username();
    }
}
