package ru.tinkoff.edu.java.scrapper.service.impl.jdbc;

import org.springframework.stereotype.Service;
import ru.tinkoff.edu.java.linkparser.model.answer.GitHubUriParserAnswer;
import ru.tinkoff.edu.java.linkparser.model.answer.NotMatchedUriParserAnswer;
import ru.tinkoff.edu.java.linkparser.model.answer.StackOverflowUriParserAnswer;
import ru.tinkoff.edu.java.linkparser.parser.UriParsersChain;
import ru.tinkoff.edu.java.scrapper.domain.model.Link;
import ru.tinkoff.edu.java.scrapper.domain.repository.JdbcLinkRepository;
import ru.tinkoff.edu.java.scrapper.service.api.LinkUpdater;
import ru.tinkoff.edu.java.scrapper.webclient.api.BotClient;
import ru.tinkoff.edu.java.scrapper.webclient.api.GitHubClient;
import ru.tinkoff.edu.java.scrapper.webclient.api.StackOverflowClient;
import ru.tinkoff.edu.java.scrapper.webclient.model.GitHubApiResponse;
import ru.tinkoff.edu.java.scrapper.webclient.model.StackOverflowItemApiResponse;

import java.sql.Timestamp;
import java.time.Instant;

@Service
public class JdbcLinkUpdater implements LinkUpdater {

    private final JdbcLinkRepository linkRepository;
    private final GitHubClient gitHubClient;
    private final StackOverflowClient stackOverflowClient;
    private final BotClient botClient;
    private final UriParsersChain uriParsersChain;

    public JdbcLinkUpdater(
            JdbcLinkRepository linkRepository,
            GitHubClient gitHubClient,
            StackOverflowClient stackOverflowClient,
            BotClient botClient,
            UriParsersChain uriParsersChain
    ) {
        this.linkRepository = linkRepository;
        this.gitHubClient = gitHubClient;
        this.stackOverflowClient = stackOverflowClient;
        this.botClient = botClient;
        this.uriParsersChain = uriParsersChain;
    }

    @Override
    public void update(long expiration) {
        var linksToUpdate = linkRepository.findOld(expiration, System.currentTimeMillis());
        var links = linksToUpdate.keySet();
        for (var link : links) {
            var updateTime = getUpdateTime(link);
            if (updateTime > link.updatedAt().toInstant().toEpochMilli()) {
                botClient.sendUpdate(
                        link.id(),
                        link.link(),
                        "There is an update for this link",
                        linksToUpdate.get(link)
                );
                linkRepository.save(link.link().toString(), Timestamp.from(Instant.ofEpochMilli(updateTime)));
            }
        }
    }

    private long getUpdateTime(Link link) {
        var uriParserAnswer = uriParsersChain.doParse(link.link().toString());
        return switch (uriParserAnswer) {
            case GitHubUriParserAnswer gitHubAnswer -> {
                GitHubApiResponse gitHubResponse = gitHubClient.fetchRepo(gitHubAnswer.userAndRepo());
                yield gitHubResponse.pushedAt().toInstant().toEpochMilli();
            }
            case StackOverflowUriParserAnswer stackOverflowAnswer -> {
                StackOverflowItemApiResponse stackOverflowResponse = stackOverflowClient.fetchQuestion(stackOverflowAnswer.id());
                yield stackOverflowResponse.lastActivityDate().toInstant().toEpochMilli();
            }
            case NotMatchedUriParserAnswer ignored -> 0;
        };
    }
}
