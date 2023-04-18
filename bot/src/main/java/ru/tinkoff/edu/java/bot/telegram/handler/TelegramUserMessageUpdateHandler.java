package ru.tinkoff.edu.java.bot.telegram.handler;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.tinkoff.edu.java.bot.telegram.cache.DialogsStateCache;
import ru.tinkoff.edu.java.bot.telegram.manager.CommandExecutorsManager;
import ru.tinkoff.edu.java.bot.telegram.model.BotState;
import ru.tinkoff.edu.java.bot.telegram.model.Command;

import static ru.tinkoff.edu.java.bot.telegram.model.BotState.*;

@Component
public class TelegramUserMessageUpdateHandler implements TelegramUpdateHandler<SendMessage> {
    private final CommandExecutorsManager commandExecutorsManager;
    private final DialogsStateCache dialogsStateCache;

    public TelegramUserMessageUpdateHandler(CommandExecutorsManager commandExecutorsManager, DialogsStateCache dialogsStateCache) {
        this.commandExecutorsManager = commandExecutorsManager;
        this.dialogsStateCache = dialogsStateCache;
    }

    @Override
    public SendMessage handle(Update update) {
        var text = update.getMessage().getText();
        var chatId = update.getMessage().getChatId();
        var userName = update.getMessage().getFrom().getUserName();
        var languageCode = update.getMessage().getFrom().getLanguageCode();

        var botState = getBotState(text, chatId);
        dialogsStateCache.setStateForId(chatId, botState);

        var command = new Command(chatId, userName, text, languageCode, botState);
        var response = commandExecutorsManager.processStateful(command);

        return new SendMessage(String.valueOf(chatId), response);
    }

    private BotState getBotState(String text, Long chatId) {
        return switch (text) {
            case "/start" -> START;
            case "/track" -> TRACK;
            case "/untrack" -> UNTRACK;
            case "/help" -> HELP;
            case "/list" -> LIST;
            default -> {
                var cachedState = dialogsStateCache.getStateById(chatId);
                yield cachedState.equals(NEW_LINK) || cachedState.equals(REMOVING_LINK) ? cachedState : INCORRECT;
            }
        };
    }
}

