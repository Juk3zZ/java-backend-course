package ru.tinkoff.edu.java.bot.telegram.model;

public record Command(
        long tgChatId,
        String username,
        String message,
        String languageCode,
        BotState relatedBotState
) {
}
