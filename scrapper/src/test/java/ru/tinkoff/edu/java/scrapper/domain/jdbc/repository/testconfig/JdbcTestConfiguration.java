package ru.tinkoff.edu.java.scrapper.domain.jdbc.repository.testconfig;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import ru.tinkoff.edu.java.scrapper.domain.jdbc.util.QueriesSource;

@TestConfiguration
@Profile("test")
public class JdbcTestConfiguration {

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
