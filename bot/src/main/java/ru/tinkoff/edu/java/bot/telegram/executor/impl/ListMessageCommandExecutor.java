package ru.tinkoff.edu.java.bot.telegram.executor.impl;

import org.springframework.stereotype.Component;
import ru.tinkoff.edu.java.bot.scrapperapi.ScrapperClient;
import ru.tinkoff.edu.java.bot.scrapperapi.exception.ApiClientErrorException;
import ru.tinkoff.edu.java.bot.scrapperapi.exception.ApiInternalServerErrorException;
import ru.tinkoff.edu.java.bot.scrapperapi.model.AllLinksApiResponse;
import ru.tinkoff.edu.java.bot.telegram.cache.DialogsStateCache;
import ru.tinkoff.edu.java.bot.telegram.executor.api.MessageCommandExecutor;
import ru.tinkoff.edu.java.bot.telegram.model.BotState;
import ru.tinkoff.edu.java.bot.telegram.model.Command;
import ru.tinkoff.edu.java.bot.telegram.util.LocaleMessageRepo;

import static ru.tinkoff.edu.java.bot.telegram.model.BotState.LIST;
import static ru.tinkoff.edu.java.bot.telegram.model.BotState.MAIN_MENU;

@Component
public class ListMessageCommandExecutor implements MessageCommandExecutor {

    public static final String REPLY_BAD_REQUEST_KEY = "reply.list.bad-request";
    public static final String REPLY_SERVER_ERROR_KEY = "reply.list.server-error";
    public static final String REPLY_EMPTY_KEY = "reply.list.empty";
    public static final String REPLY_KEY = "reply.list";

    private final DialogsStateCache dialogsStateCache;
    private final ScrapperClient scrapperClient;
    private final LocaleMessageRepo messageRepo;

    public ListMessageCommandExecutor(DialogsStateCache dialogsStateCache, ScrapperClient scrapperClient, LocaleMessageRepo messageRepo) {
        this.dialogsStateCache = dialogsStateCache;
        this.scrapperClient = scrapperClient;
        this.messageRepo = messageRepo;
    }

    @Override
    public BotState getSupportedType() {
        return LIST;
    }

    @Override
    public String execute(Command command) {
        AllLinksApiResponse listLinksResponse;

        dialogsStateCache.setStateForId(command.tgChatId(), MAIN_MENU);
        try {
            listLinksResponse = scrapperClient.getAllLinks(command.tgChatId());
        } catch (ApiClientErrorException e) {
            return messageRepo.getMessageByKey(REPLY_BAD_REQUEST_KEY, command.languageCode());
        } catch (ApiInternalServerErrorException e) {
            return messageRepo.getMessageByKey(REPLY_SERVER_ERROR_KEY, command.languageCode());
        }

        var builder = new StringBuilder();
        if (listLinksResponse.size() == 0) {
            builder.append(messageRepo.getMessageByKey(REPLY_EMPTY_KEY, command.languageCode()));
        } else {
            builder.append(String.format(
                    messageRepo.getMessageByKey(REPLY_KEY, command.languageCode()),
                    listLinksResponse.size())
            );
            listLinksResponse.links().forEach(x -> builder.append(x.url()).append("\n"));
        }

        return builder.toString();
    }
}
