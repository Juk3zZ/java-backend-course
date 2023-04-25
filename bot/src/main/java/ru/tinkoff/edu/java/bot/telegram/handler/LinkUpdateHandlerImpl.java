package ru.tinkoff.edu.java.bot.telegram.handler;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import ru.tinkoff.edu.java.bot.model.LinkUpdateType;
import ru.tinkoff.edu.java.bot.telegram.util.LocaleMessageRepo;

@Component
public class LinkUpdateHandlerImpl implements LinkUpdateHandler {

    public static final String REPLY_COMMON_KEY = "reply.link-update.common";
    public static final String REPLY_GITHUB_ISSUES_KEY = "reply.link-update.github-issues";
    public static final String REPLY_STACKOVERFLOW_ANSWERS_KEY = "reply.link-update.stackoverflow-answers";

    private final LocaleMessageRepo messageRepo;

    public LinkUpdateHandlerImpl(LocaleMessageRepo messageRepo) {
        this.messageRepo = messageRepo;
    }

    @Override
    public SendMessage handle(String url, LinkUpdateType updateType, Long tgChatId) {
        var message = switch (updateType) {
            case COMMON -> messageRepo.getMessageByKey(REPLY_COMMON_KEY, "ru");
            case GITHUB_ISSUES -> messageRepo.getMessageByKey(REPLY_GITHUB_ISSUES_KEY, "ru");
            case STACKOVERFLOW_ANSWERS -> messageRepo.getMessageByKey(REPLY_STACKOVERFLOW_ANSWERS_KEY, "ru");
        };
        return new SendMessage(String.valueOf(tgChatId), String.format(message, url));
    }
}
