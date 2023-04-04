package ru.tinkoff.edu.java.bot.telegram.manager;

import org.springframework.stereotype.Component;
import ru.tinkoff.edu.java.bot.telegram.executor.api.MessageCommandExecutor;
import ru.tinkoff.edu.java.bot.telegram.model.BotState;
import ru.tinkoff.edu.java.bot.telegram.model.Command;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class CommandExecutorsManager {

    private final Map<BotState, MessageCommandExecutor> messageExecutors = new HashMap<>();

    public CommandExecutorsManager(List<MessageCommandExecutor> executors) {
        executors.forEach(executor -> messageExecutors.put(executor.getSupportedType(), executor));
    }

    public String processStateful(Command command) {
        return messageExecutors.get(command.relatedBotState()).execute(command);
    }
}
