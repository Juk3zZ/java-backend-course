package ru.tinkoff.edu.java.bot.telegram.cache;

import org.springframework.stereotype.Component;
import ru.tinkoff.edu.java.bot.telegram.model.BotState;

import java.util.HashMap;
import java.util.Map;

@Component
public class InMemoryDialogsStateCache implements DialogsStateCache {

    private final Map<Long, BotState> chatsStates = new HashMap<>();

    @Override
    public BotState getStateById(long tgChatId) {
        var botState = chatsStates.get(tgChatId);
        if (botState == null) botState = BotState.MAIN_MENU;
        return botState;
    }

    @Override
    public void setStateForId(long tgChatId, BotState botState) {
        chatsStates.put(tgChatId, botState);
    }
}
