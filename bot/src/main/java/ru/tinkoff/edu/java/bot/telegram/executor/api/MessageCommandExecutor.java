package ru.tinkoff.edu.java.bot.telegram.executor.api;

import ru.tinkoff.edu.java.bot.telegram.model.BotState;
import ru.tinkoff.edu.java.bot.telegram.model.Command;

public interface MessageCommandExecutor {

    BotState getSupportedType();

    String execute(Command command);
}
