package ru.tinkoff.edu.java.scrapper.configuration.accesstype;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jooq.DSLContext;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.tinkoff.edu.java.linkparser.parser.UriParsersChain;
import ru.tinkoff.edu.java.scrapper.domain.jooq.mapper.LinkFieldsMapper;
import ru.tinkoff.edu.java.scrapper.domain.jooq.repository.JooqChatRepository;
import ru.tinkoff.edu.java.scrapper.domain.jooq.repository.JooqLinkRepository;
import ru.tinkoff.edu.java.scrapper.domain.util.MappingUtils;
import ru.tinkoff.edu.java.scrapper.service.impl.jooq.JooqChatService;
import ru.tinkoff.edu.java.scrapper.service.impl.jooq.JooqLinkService;
import ru.tinkoff.edu.java.scrapper.service.impl.jooq.JooqLinkUpdater;
import ru.tinkoff.edu.java.scrapper.webclient.api.BotClient;
import ru.tinkoff.edu.java.scrapper.webclient.api.GitHubClient;
import ru.tinkoff.edu.java.scrapper.webclient.api.StackOverflowClient;

@Configuration
@ConditionalOnProperty(prefix = "app", name = "access-type", havingValue = "jooq")
public class JooqAccessConfiguration {

    @Bean
    public JooqChatRepository jooqChatRepository(DSLContext dslContext) {
        return new JooqChatRepository(dslContext);
    }

    @Bean
    public JooqLinkRepository jooqLinkRepository(
            DSLContext dslContext,
            MappingUtils mappingUtils,
            ObjectMapper objectMapper,
            LinkFieldsMapper linkFieldsMapper
    ) {
        return new JooqLinkRepository(dslContext, mappingUtils, objectMapper, linkFieldsMapper);
    }

    @Bean
    public JooqChatService jooqChatService(JooqChatRepository jooqChatRepository) {
        return new JooqChatService(jooqChatRepository);
    }

    @Bean
    public JooqLinkService jooqLinkService(JooqLinkRepository jooqLinkRepository) {
        return new JooqLinkService(jooqLinkRepository);
    }

    @Bean
    public JooqLinkUpdater jooqLinkUpdater(
            JooqLinkRepository linkRepository,
            GitHubClient gitHubClient,
            StackOverflowClient stackOverflowClient,
            BotClient botClient,
            UriParsersChain uriParsersChain
    ) {
        return new JooqLinkUpdater(linkRepository, gitHubClient, stackOverflowClient, botClient, uriParsersChain);
    }
}
