package ru.tinkoff.edu.java.bot.telegram.cache;

import ru.tinkoff.edu.java.bot.telegram.model.BotState;

public interface DialogsStateCache {

    BotState getStateById(long tgChatId);

    void setStateForId(long tgChatId, BotState botState);
}
