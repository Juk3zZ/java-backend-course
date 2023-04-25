package ru.tinkoff.edu.java.bot.telegram.handler;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import ru.tinkoff.edu.java.bot.model.LinkUpdateType;

public interface LinkUpdateHandler {

    SendMessage handle(String url, LinkUpdateType updateType, Long tgChatId);
}
