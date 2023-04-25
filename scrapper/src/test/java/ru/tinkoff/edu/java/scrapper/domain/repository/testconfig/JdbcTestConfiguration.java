package ru.tinkoff.edu.java.scrapper.domain.repository.testconfig;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionManager;
import ru.tinkoff.edu.java.scrapper.domain.util.QueriesSource;

import javax.sql.DataSource;

@TestConfiguration
@Profile("test")
public class JdbcTestConfiguration {

    @Bean
    public JdbcTemplate pgJdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean
    public TransactionManager transactionManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean(name = "queriesMessageSource")
    @Primary
    public MessageSource messageSource() {
        var messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setBasename("classpath:queries");
        messageSource.setDefaultEncoding("UTF-8");
        return messageSource;
    }
    @Bean
    public QueriesSource queriesSource(@Qualifier("queriesMessageSource") MessageSource messageSource) {
        return new QueriesSource(messageSource);
    }
}
