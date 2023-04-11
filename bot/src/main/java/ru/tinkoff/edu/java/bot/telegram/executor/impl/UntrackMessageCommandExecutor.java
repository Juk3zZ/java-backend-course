package ru.tinkoff.edu.java.bot.telegram.executor.impl;

import org.springframework.stereotype.Component;
import ru.tinkoff.edu.java.bot.telegram.cache.DialogsStateCache;
import ru.tinkoff.edu.java.bot.telegram.executor.api.MessageCommandExecutor;
import ru.tinkoff.edu.java.bot.telegram.model.BotState;
import ru.tinkoff.edu.java.bot.telegram.model.Command;
import ru.tinkoff.edu.java.bot.telegram.util.LocaleMessageRepo;

import static ru.tinkoff.edu.java.bot.telegram.model.BotState.REMOVING_LINK;
import static ru.tinkoff.edu.java.bot.telegram.model.BotState.UNTRACK;

@Component
public class UntrackMessageCommandExecutor implements MessageCommandExecutor {

    public static final String REPLY_KEY = "reply.untrack";

    private final DialogsStateCache dialogsStateCache;
    private final LocaleMessageRepo messageRepo;

    public UntrackMessageCommandExecutor(DialogsStateCache dialogsStateCache, LocaleMessageRepo messageRepo) {
        this.dialogsStateCache = dialogsStateCache;
        this.messageRepo = messageRepo;
    }

    @Override
    public BotState getSupportedType() {
        return UNTRACK;
    }

    @Override
    public String execute(Command command) {
        dialogsStateCache.setStateForId(command.tgChatId(), REMOVING_LINK);
        return messageRepo.getMessageByKey(REPLY_KEY, command.languageCode());
    }
}
