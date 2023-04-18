package ru.tinkoff.edu.java.scrapper.domain;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionManager;
import ru.tinkoff.edu.java.scrapper.domain.util.MappingUtils;

import javax.sql.DataSource;

@TestConfiguration
@Profile("test")
public class CommonConfig {
    @Bean
    public JdbcTemplate pgJdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean
    public TransactionManager transactionManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean
    public MappingUtils mappingUtils() {
        return new MappingUtils();
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}
