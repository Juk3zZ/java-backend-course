package ru.tinkoff.edu.java.bot.telegram.executor.impl;

import org.springframework.stereotype.Component;
import ru.tinkoff.edu.java.bot.scrapperapi.ScrapperClient;
import ru.tinkoff.edu.java.bot.scrapperapi.exception.ApiClientErrorException;
import ru.tinkoff.edu.java.bot.scrapperapi.exception.ApiInternalServerErrorException;
import ru.tinkoff.edu.java.bot.telegram.cache.DialogsStateCache;
import ru.tinkoff.edu.java.bot.telegram.executor.api.MessageCommandExecutor;
import ru.tinkoff.edu.java.bot.telegram.model.BotState;
import ru.tinkoff.edu.java.bot.telegram.model.Command;
import ru.tinkoff.edu.java.bot.telegram.util.LocaleMessageRepo;

import static ru.tinkoff.edu.java.bot.telegram.model.BotState.MAIN_MENU;
import static ru.tinkoff.edu.java.bot.telegram.model.BotState.START;

@Component
public class StartMessageCommandExecutor implements MessageCommandExecutor {

    public static final String REPLY_SERVER_ERROR_KEY = "reply.start.server-error";
    public static final String REPLY_BAD_REQUEST_KEY = "reply.start.bad-request";
    public static final String REPLY_KEY = "reply.start";

    private final DialogsStateCache dialogsStateCache;
    private final ScrapperClient scrapperClient;
    private final LocaleMessageRepo messageRepo;

    public StartMessageCommandExecutor(DialogsStateCache dialogsStateCache, ScrapperClient scrapperClient, LocaleMessageRepo messageRepo) {
        this.dialogsStateCache = dialogsStateCache;
        this.scrapperClient = scrapperClient;
        this.messageRepo = messageRepo;
    }

    @Override
    public BotState getSupportedType() {
        return START;
    }

    @Override
    public String execute(Command command) {
        try {
            scrapperClient.registerChat(command.tgChatId(), command.username());
        } catch (ApiInternalServerErrorException e) {
            return messageRepo.getMessageByKey(REPLY_SERVER_ERROR_KEY, command.languageCode());
        } catch (ApiClientErrorException e) {
            return messageRepo.getMessageByKey(REPLY_BAD_REQUEST_KEY, command.languageCode());
        }
        dialogsStateCache.setStateForId(command.tgChatId(), MAIN_MENU);
        return messageRepo.getMessageByKey(REPLY_KEY, command.languageCode());
    }
}
