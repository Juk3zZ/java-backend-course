package ru.tinkoff.edu.java.bot.telegram.executor.impl;

import org.springframework.stereotype.Component;
import ru.tinkoff.edu.java.bot.telegram.cache.DialogsStateCache;
import ru.tinkoff.edu.java.bot.telegram.executor.api.MessageCommandExecutor;
import ru.tinkoff.edu.java.bot.telegram.model.BotState;
import ru.tinkoff.edu.java.bot.telegram.model.Command;
import ru.tinkoff.edu.java.bot.telegram.util.LocaleMessageRepo;

import static ru.tinkoff.edu.java.bot.telegram.model.BotState.TRACK;
import static ru.tinkoff.edu.java.bot.telegram.model.BotState.NEW_LINK;

@Component
public class TrackMessageCommandExecutor implements MessageCommandExecutor {

    public static final String REPLY_KEY = "reply.track";

    private final DialogsStateCache dialogsStateCache;
    private final LocaleMessageRepo messageRepo;

    public TrackMessageCommandExecutor(DialogsStateCache dialogsStateCache, LocaleMessageRepo messageRepo) {
        this.dialogsStateCache = dialogsStateCache;
        this.messageRepo = messageRepo;
    }

    @Override
    public BotState getSupportedType() {
        return TRACK;
    }

    @Override
    public String execute(Command command) {
        dialogsStateCache.setStateForId(command.tgChatId(), NEW_LINK);
        return messageRepo.getMessageByKey(REPLY_KEY, command.languageCode());
    }
}
