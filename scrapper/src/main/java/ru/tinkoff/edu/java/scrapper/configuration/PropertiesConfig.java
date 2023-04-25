package ru.tinkoff.edu.java.scrapper.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PropertiesConfig {

    @Bean
    public long schedulerIntervalMs(ApplicationConfig config) {
        return config.scheduler().interval().toMillis();
    }

    @Bean
    public long expirationIntervalMs(ApplicationConfig config) {
        return config.expiration().interval().toMillis();
    }

    @Bean
    public String url(ApplicationConfig config) {
        return config.db().url();
    }

    @Bean
    public String user(ApplicationConfig config) {
        return config.db().user();
    }

    @Bean
    public String password(ApplicationConfig config) {
        return config.db().password();
    }
}