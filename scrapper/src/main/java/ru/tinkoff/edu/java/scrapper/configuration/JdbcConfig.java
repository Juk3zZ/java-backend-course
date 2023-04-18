package ru.tinkoff.edu.java.scrapper.configuration;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import ru.tinkoff.edu.java.linkparser.parser.UriParsersChain;
import ru.tinkoff.edu.java.linkparser.parser.impl.GitHubUriParser;
import ru.tinkoff.edu.java.linkparser.parser.impl.StackOverflowUriParser;
import ru.tinkoff.edu.java.linkparser.parser.impl.UnsupportedUriParser;

import javax.sql.DataSource;

@Configuration
@EnableTransactionManagement
@Profile("!test")
public class JdbcConfig {

    @Bean
    public DataSource postgreSqlDataSource(String url, String user, String password) {
        var dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.postgresql.Driver");
        dataSource.setUrl(url);
        dataSource.setUsername(user);
        dataSource.setPassword(password);
        return dataSource;
    }

    @Bean
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean
    @Primary
    public MessageSource messageSource() {
        var messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setBasename("classpath:queries");
        messageSource.setDefaultEncoding("UTF-8");
        return messageSource;
    }

    @Bean
    public UriParsersChain uriParsersChain() {
        return new UriParsersChain(
                new GitHubUriParser("github.com"),
                new StackOverflowUriParser("stackoverflow.com"),
                new UnsupportedUriParser()
        );
    }

}
