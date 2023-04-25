package ru.tinkoff.edu.java.scrapper.configuration.accesstype;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.tinkoff.edu.java.linkparser.parser.UriParsersChain;
import ru.tinkoff.edu.java.scrapper.domain.jdbc.mapper.ChatMapper;
import ru.tinkoff.edu.java.scrapper.domain.jdbc.mapper.LinkMapper;
import ru.tinkoff.edu.java.scrapper.domain.jdbc.repository.JdbcChatRepository;
import ru.tinkoff.edu.java.scrapper.domain.jdbc.repository.JdbcLinkRepository;
import ru.tinkoff.edu.java.scrapper.domain.jdbc.util.QueriesSource;
import ru.tinkoff.edu.java.scrapper.domain.util.MappingUtils;
import ru.tinkoff.edu.java.scrapper.service.impl.jdbc.JdbcChatService;
import ru.tinkoff.edu.java.scrapper.service.impl.jdbc.JdbcLinkService;
import ru.tinkoff.edu.java.scrapper.service.impl.jdbc.JdbcLinkUpdater;
import ru.tinkoff.edu.java.scrapper.webclient.api.BotClient;
import ru.tinkoff.edu.java.scrapper.webclient.api.GitHubClient;
import ru.tinkoff.edu.java.scrapper.webclient.api.StackOverflowClient;

@Configuration
@ConditionalOnProperty(prefix = "app", name = "access-type", havingValue = "jdbc")
public class JdbcAccessConfiguration {

    @Bean
    @Profile("!test")
    public JdbcChatRepository jdbcChatRepository(JdbcTemplate jdbcTemplate, ChatMapper chatMapper, QueriesSource queriesSource) {
        return new JdbcChatRepository(jdbcTemplate, chatMapper, queriesSource);
    }

    @Bean
    @Profile("!test")
    public JdbcLinkRepository jdbcLinkRepository(
            JdbcTemplate jdbcTemplate,
            LinkMapper linkMapper,
            QueriesSource queriesSource,
            MappingUtils mappingUtils,
            ObjectMapper objectMapper
    ) {
        return new JdbcLinkRepository(jdbcTemplate, linkMapper, queriesSource, mappingUtils, objectMapper);
    }

    @Bean
    @Profile("!test")
    public JdbcChatService jdbcChatService(JdbcChatRepository jdbcChatRepository) {
        return new JdbcChatService(jdbcChatRepository);
    }

    @Bean
    @Profile("!test")
    public JdbcLinkService jdbcLinkService(JdbcLinkRepository jdbcLinkRepository) {
        return new JdbcLinkService(jdbcLinkRepository);
    }

    @Bean
    @Profile("!test")
    public JdbcLinkUpdater jdbcLinkUpdater(
            JdbcLinkRepository linkRepository,
            GitHubClient gitHubClient,
            StackOverflowClient stackOverflowClient,
            BotClient botClient,
            UriParsersChain uriParsersChain
    ) {
        return new JdbcLinkUpdater(linkRepository, gitHubClient, stackOverflowClient, botClient, uriParsersChain);
    }
}
