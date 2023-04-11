package ru.tinkoff.edu.java.bot.telegram.executor.impl;

import org.springframework.stereotype.Component;
import ru.tinkoff.edu.java.bot.scrapperapi.ScrapperClient;
import ru.tinkoff.edu.java.bot.scrapperapi.exception.ApiClientErrorException;
import ru.tinkoff.edu.java.bot.scrapperapi.exception.ApiInternalServerErrorException;
import ru.tinkoff.edu.java.bot.scrapperapi.model.LinkResponse;
import ru.tinkoff.edu.java.bot.telegram.cache.DialogsStateCache;
import ru.tinkoff.edu.java.bot.telegram.executor.api.MessageCommandExecutor;
import ru.tinkoff.edu.java.bot.telegram.model.BotState;
import ru.tinkoff.edu.java.bot.telegram.model.Command;
import ru.tinkoff.edu.java.bot.telegram.util.LocaleMessageRepo;

import static ru.tinkoff.edu.java.bot.telegram.model.BotState.*;

@Component
public class RemovingLinkMessageCommandExecutor implements MessageCommandExecutor {

    public static final String REPLY_BAD_REQUEST_KEY = "reply.remove-link.bad-request";
    public static final String REPLY_SERVER_ERROR_KEY = "reply.remove-link.server-error";
    public static final String REPLY_KEY = "reply.remove-link";

    private final DialogsStateCache dialogsStateCache;
    private final ScrapperClient scrapperClient;
    private final LocaleMessageRepo messageRepo;

    public RemovingLinkMessageCommandExecutor(DialogsStateCache dialogsStateCache, ScrapperClient scrapperClient, LocaleMessageRepo messageRepo) {
        this.dialogsStateCache = dialogsStateCache;
        this.scrapperClient = scrapperClient;
        this.messageRepo = messageRepo;
    }

    @Override
    public BotState getSupportedType() {
        return REMOVING_LINK;
    }

    @Override
    public String execute(Command command) {
        LinkResponse linkResponse;
        try {
            linkResponse = scrapperClient.deleteLink(command.tgChatId(), command.message());
        } catch (ApiClientErrorException e) {
            return messageRepo.getMessageByKey(REPLY_BAD_REQUEST_KEY, command.languageCode());
        } catch (ApiInternalServerErrorException e) {
            return messageRepo.getMessageByKey(REPLY_SERVER_ERROR_KEY, command.languageCode());
        } finally {
            dialogsStateCache.setStateForId(command.tgChatId(), MAIN_MENU);
        }
        return String.format(messageRepo.getMessageByKey(REPLY_KEY, command.languageCode()), linkResponse.url());
    }
}
