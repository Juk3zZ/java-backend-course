package ru.tinkoff.edu.java.bot.telegram.executor.impl;

//import org.springframework.stereotype.Component;
//import ru.tinkoff.edu.java.bot.scrapperapi.ScrapperClient;
//import ru.tinkoff.edu.java.bot.scrapperapi.exception.ApiClientErrorException;
//import ru.tinkoff.edu.java.bot.scrapperapi.exception.ApiInternalServerErrorException;
//import ru.tinkoff.edu.java.bot.scrapperapi.model.LinkResponse;
//import ru.tinkoff.edu.java.bot.telegram.cache.DialogsStateCache;
//import ru.tinkoff.edu.java.bot.telegram.executor.api.MessageCommandExecutor;
//import ru.tinkoff.edu.java.bot.telegram.model.BotState;
//import ru.tinkoff.edu.java.bot.telegram.model.Command;
//import ru.tinkoff.edu.java.bot.telegram.util.LocaleMessageRepo;
//
//import static ru.tinkoff.edu.java.bot.telegram.model.BotState.MAIN_MENU;
//import static ru.tinkoff.edu.java.bot.telegram.model.BotState.NEW_LINK;

import org.springframework.stereotype.Component;
import ru.tinkoff.edu.java.bot.telegram.cache.DialogsStateCache;
import ru.tinkoff.edu.java.bot.telegram.executor.api.MessageCommandExecutor;
import ru.tinkoff.edu.java.bot.telegram.model.BotState;
import ru.tinkoff.edu.java.bot.telegram.model.Command;
import ru.tinkoff.edu.java.bot.telegram.util.LocaleMessageRepo;

import static ru.tinkoff.edu.java.bot.telegram.model.BotState.TRACK;
import static ru.tinkoff.edu.java.bot.telegram.model.BotState.MAIN_MENU;
import static ru.tinkoff.edu.java.bot.telegram.model.BotState.NEW_LINK;

@Component
public class NewLinkMessageCommandExecutor implements MessageCommandExecutor {


    public static final String REPLY_KEY = "reply.new-link";
    public static final String REPLY_SERVER_ERROR_KEY = "reply.new-link.server-error";

    private final DialogsStateCache dialogsStateCache;
    private final LocaleMessageRepo messageRepo;

    public NewLinkMessageCommandExecutor(DialogsStateCache dialogsStateCache, LocaleMessageRepo messageRepo) {
        this.dialogsStateCache = dialogsStateCache;
        this.messageRepo = messageRepo;
    }

    @Override
    public BotState getSupportedType() {
        return NEW_LINK;
    }

    @Override
    public String execute(Command command) {
        if (command.toString().contains("https://")) {
            dialogsStateCache.setStateForId(command.tgChatId(), MAIN_MENU);
            return messageRepo.getMessageByKey(REPLY_KEY, command.languageCode());
        } else {
            dialogsStateCache.setStateForId(command.tgChatId(), NEW_LINK);
            return messageRepo.getMessageByKey(REPLY_SERVER_ERROR_KEY, command.languageCode());
        }

    }

//    public static final String REPLY_BAD_REQUEST_KEY = "reply.new-link.bad-request";
//    public static final String REPLY_SERVER_ERROR_KEY = "reply.new-link.server-error";
//    public static final String REPLY_KEY = "reply.new-link";
//
//    private final DialogsStateCache dialogsStateCache;
//    private final ScrapperClient scrapperClient;
//    private final LocaleMessageRepo messageRepo;
//
//    public NewLinkMessageCommandExecutor(DialogsStateCache dialogsStateCache, ScrapperClient scrapperClient, LocaleMessageRepo messageRepo) {
//        this.dialogsStateCache = dialogsStateCache;
//        this.scrapperClient = scrapperClient;
//        this.messageRepo = messageRepo;
//    }
//
//    @Override
//    public BotState getSupportedType() {
//        return NEW_LINK;
//    }
//
//    @Override
//    public String execute(Command command) {
//        LinkResponse linkResponse;
//        try {
//            linkResponse = scrapperClient.addLink(command.tgChatId(), command.message());
//        } catch (ApiClientErrorException e) {
//            return messageRepo.getMessageByKey(REPLY_BAD_REQUEST_KEY, command.languageCode());
//        } catch (ApiInternalServerErrorException e) {
//            return messageRepo.getMessageByKey(REPLY_SERVER_ERROR_KEY, command.languageCode());
//        } finally {
//            dialogsStateCache.setStateForId(command.tgChatId(), MAIN_MENU);
//        }
//        return String.format(messageRepo.getMessageByKey(REPLY_KEY, command.languageCode()), linkResponse.url());
//    }
}
