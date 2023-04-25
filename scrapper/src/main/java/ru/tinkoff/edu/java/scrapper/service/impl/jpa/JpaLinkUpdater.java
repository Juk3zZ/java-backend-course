package ru.tinkoff.edu.java.scrapper.service.impl.jpa;

import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import ru.tinkoff.edu.java.linkparser.model.answer.GitHubUriParserAnswer;
import ru.tinkoff.edu.java.linkparser.model.answer.NotMatchedUriParserAnswer;
import ru.tinkoff.edu.java.linkparser.model.answer.StackOverflowUriParserAnswer;
import ru.tinkoff.edu.java.linkparser.parser.UriParsersChain;
import ru.tinkoff.edu.java.scrapper.domain.jpa.repository.JpaLinkRepository;
import ru.tinkoff.edu.java.scrapper.domain.model.jpa.JpaChat;
import ru.tinkoff.edu.java.scrapper.domain.model.jpa.JpaLink;
import ru.tinkoff.edu.java.scrapper.service.api.LinkUpdater;
import ru.tinkoff.edu.java.scrapper.webclient.api.BotClient;
import ru.tinkoff.edu.java.scrapper.webclient.api.GitHubClient;
import ru.tinkoff.edu.java.scrapper.webclient.api.StackOverflowClient;

import java.net.URI;
import java.sql.Timestamp;
import java.time.Instant;

import static ru.tinkoff.edu.java.scrapper.model.request.LinkUpdateType.*;

@Transactional
@RequiredArgsConstructor
public class JpaLinkUpdater implements LinkUpdater {

    private final JpaLinkRepository linkRepository;
    private final GitHubClient gitHubClient;
    private final StackOverflowClient stackOverflowClient;
    private final BotClient botClient;
    private final UriParsersChain uriParsersChain;

    @Override
    public void update(long expiration) {
        var timeBorder = System.currentTimeMillis() - expiration;
        var oldLinks = linkRepository.findByUpdatedAtLessThan(new Timestamp(timeBorder));
        oldLinks.forEach(this::updateLink);
    }

    private void updateLink(JpaLink link) {
        var uriParserAnswer = uriParsersChain.doParse(link.getLink());
        switch (uriParserAnswer) {
            case GitHubUriParserAnswer gitHubAnswer -> updateGitHubLink(link, gitHubAnswer);
            case StackOverflowUriParserAnswer stackOverflowAnswer -> updateStackOverflowLink(link, stackOverflowAnswer);
            case NotMatchedUriParserAnswer ignored -> { }
        }
    }

    private void updateGitHubLink(JpaLink link, GitHubUriParserAnswer gitHubAnswer) {
        var gitHubResponse = gitHubClient.fetchRepo(gitHubAnswer.userAndRepo());
        var updateTime = gitHubResponse.pushedAt().toInstant().toEpochMilli();
        var openIssuesCount = link.getUpdateInfo().get("open_issues_count");
        link.getUpdateInfo().put("open_issues_count", gitHubResponse.openIssuesCount());

        if (openIssuesCount != null && gitHubResponse.openIssuesCount() != (int) openIssuesCount) {
            botClient.sendUpdate(
                    link.getId(),
                    URI.create(link.getLink()),
                    GITHUB_ISSUES,
                    link.getTrackingJpaChats().stream().map(JpaChat::getTgChatId).toList()
            );
        } else if (updateTime > link.getUpdatedAt().toInstant().toEpochMilli()) {
            botClient.sendUpdate(
                    link.getId(),
                    URI.create(link.getLink()),
                    COMMON,
                    link.getTrackingJpaChats().stream().map(JpaChat::getTgChatId).toList()
            );
        }
        link.setUpdatedAt(Timestamp.from(Instant.ofEpochMilli(updateTime)));
        linkRepository.save(link);
    }

    private void updateStackOverflowLink(JpaLink link, StackOverflowUriParserAnswer stackOverflowAnswer) {
        var stackOverflowResponse = stackOverflowClient.fetchQuestion(stackOverflowAnswer.id());
        var updateTime = stackOverflowResponse.lastActivityDate().toInstant().toEpochMilli();
        var answerCount = link.getUpdateInfo().get("answer_count");
        link.getUpdateInfo().put("answer_count", stackOverflowResponse.answerCount());

        if (answerCount != null && stackOverflowResponse.answerCount() != (int) answerCount) {
            botClient.sendUpdate(
                    link.getId(),
                    URI.create(link.getLink()),
                    STACKOVERFLOW_ANSWERS,
                    link.getTrackingJpaChats().stream().map(JpaChat::getTgChatId).toList()
            );
        } else if (updateTime > link.getUpdatedAt().toInstant().toEpochMilli()) {
            botClient.sendUpdate(
                    link.getId(),
                    URI.create(link.getLink()),
                    COMMON,
                    link.getTrackingJpaChats().stream().map(JpaChat::getTgChatId).toList()
            );
        }
        link.setUpdatedAt(Timestamp.from(Instant.ofEpochMilli(updateTime)));
        linkRepository.save(link);
    }
}
