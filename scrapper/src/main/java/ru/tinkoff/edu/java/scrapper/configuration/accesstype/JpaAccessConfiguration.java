package ru.tinkoff.edu.java.scrapper.configuration.accesstype;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.tinkoff.edu.java.linkparser.parser.UriParsersChain;
import ru.tinkoff.edu.java.scrapper.domain.jpa.repository.JpaChatRepository;
import ru.tinkoff.edu.java.scrapper.domain.jpa.repository.JpaLinkRepository;
import ru.tinkoff.edu.java.scrapper.service.impl.jpa.JpaChatService;
import ru.tinkoff.edu.java.scrapper.service.impl.jpa.JpaLinkService;
import ru.tinkoff.edu.java.scrapper.service.impl.jpa.JpaLinkUpdater;
import ru.tinkoff.edu.java.scrapper.webclient.api.BotClient;
import ru.tinkoff.edu.java.scrapper.webclient.api.GitHubClient;
import ru.tinkoff.edu.java.scrapper.webclient.api.StackOverflowClient;

@Configuration
@ConditionalOnProperty(prefix = "app", name = "access-type", havingValue = "jpa")
public class JpaAccessConfiguration {

    @Bean
    public JpaChatService jpaChatService(JpaChatRepository jpaChatRepository) {
        return new JpaChatService(jpaChatRepository);
    }

    @Bean
    public JpaLinkService jpaLinkService(JpaLinkRepository jpaLinkRepository, JpaChatRepository jpaChatRepository) {
        return new JpaLinkService(jpaLinkRepository, jpaChatRepository);
    }

    @Bean
    public JpaLinkUpdater jpaLinkUpdater(
            JpaLinkRepository jpaLinkRepository,
            GitHubClient gitHubClient,
            StackOverflowClient stackOverflowClient,
            BotClient botClient,
            UriParsersChain uriParsersChain
    ) {
        return new JpaLinkUpdater(jpaLinkRepository, gitHubClient, stackOverflowClient, botClient, uriParsersChain);
    }
}
