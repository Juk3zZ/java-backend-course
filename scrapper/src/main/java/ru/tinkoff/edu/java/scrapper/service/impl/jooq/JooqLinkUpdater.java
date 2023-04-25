package ru.tinkoff.edu.java.scrapper.service.impl.jooq;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.tinkoff.edu.java.linkparser.model.answer.GitHubUriParserAnswer;
import ru.tinkoff.edu.java.linkparser.model.answer.NotMatchedUriParserAnswer;
import ru.tinkoff.edu.java.linkparser.model.answer.StackOverflowUriParserAnswer;
import ru.tinkoff.edu.java.linkparser.parser.UriParsersChain;
import ru.tinkoff.edu.java.scrapper.domain.jooq.repository.JooqLinkRepository;
import ru.tinkoff.edu.java.scrapper.domain.model.Link;
import ru.tinkoff.edu.java.scrapper.service.api.LinkUpdater;
import ru.tinkoff.edu.java.scrapper.webclient.api.BotClient;
import ru.tinkoff.edu.java.scrapper.webclient.api.GitHubClient;
import ru.tinkoff.edu.java.scrapper.webclient.api.StackOverflowClient;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

import static ru.tinkoff.edu.java.scrapper.model.request.LinkUpdateType.*;

@Service
@Transactional
@Primary
public class JooqLinkUpdater implements LinkUpdater {

    private final JooqLinkRepository linkRepository;
    private final GitHubClient gitHubClient;
    private final StackOverflowClient stackOverflowClient;
    private final BotClient botClient;
    private final UriParsersChain uriParsersChain;

    public JooqLinkUpdater(
            JooqLinkRepository linkRepository,
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
        var linksWithTgChatIds = linkRepository.findOld(expiration, System.currentTimeMillis());
        var links = linksWithTgChatIds.keySet();
        for (var link : links) updateLink(link, linksWithTgChatIds.get(link));
    }

    private void updateLink(Link link, List<Long> tgChatIds) {
        var uriParserAnswer = uriParsersChain.doParse(link.link().toString());
        switch (uriParserAnswer) {
            case GitHubUriParserAnswer gitHubAnswer -> updateGitHubLink(link, tgChatIds, gitHubAnswer);
            case StackOverflowUriParserAnswer stackOverflowAnswer -> updateStackOverflowLink(link, tgChatIds, stackOverflowAnswer);
            case NotMatchedUriParserAnswer ignored -> { }
        }
    }

    private void updateGitHubLink(Link link, List<Long> tgChatIds, GitHubUriParserAnswer gitHubAnswer) {
        var gitHubResponse = gitHubClient.fetchRepo(gitHubAnswer.userAndRepo());
        var updateTime = gitHubResponse.pushedAt().toInstant().toEpochMilli();
        var openIssuesCount = link.updateInfo().get("open_issues_count"); //сценарий проверки issues
        link.updateInfo().put("open_issues_count", gitHubResponse.openIssuesCount());

        if (openIssuesCount != null && gitHubResponse.openIssuesCount() != (int) openIssuesCount) {
            botClient.sendUpdate(link.id(), link.link(), GITHUB_ISSUES, tgChatIds);
            updateTime = System.currentTimeMillis();
        } else if (updateTime > link.updatedAt().toInstant().toEpochMilli()) {
            botClient.sendUpdate(link.id(), link.link(), COMMON, tgChatIds);
        }
        linkRepository.save(link.link().toString(), Timestamp.from(Instant.ofEpochMilli(updateTime)), link.updateInfo());
    }

    private void updateStackOverflowLink(Link link, List<Long> tgChatIds, StackOverflowUriParserAnswer stackOverflowAnswer) {
        var stackOverflowResponse = stackOverflowClient.fetchQuestion(stackOverflowAnswer.id());
        var updateTime = stackOverflowResponse.lastActivityDate().toInstant().toEpochMilli();
        var answerCount = link.updateInfo().get("answer_count"); //сценарий проверки количества ответов
        link.updateInfo().put("answer_count", stackOverflowResponse.answerCount());

        if (answerCount != null && stackOverflowResponse.answerCount() != (int) answerCount) {
            botClient.sendUpdate(link.id(), link.link(), STACKOVERFLOW_ANSWERS, tgChatIds);
            updateTime = System.currentTimeMillis();
        } else if (updateTime > link.updatedAt().toInstant().toEpochMilli()) {
            botClient.sendUpdate(link.id(), link.link(), COMMON, tgChatIds);
        }
        linkRepository.save(link.link().toString(), Timestamp.from(Instant.ofEpochMilli(updateTime)), link.updateInfo());
    }
}
