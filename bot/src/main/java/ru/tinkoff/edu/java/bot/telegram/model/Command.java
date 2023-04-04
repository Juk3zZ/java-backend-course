package ru.tinkoff.edu.java.bot.telegram.model;

public record Command(
        long tgChatId,
        String message,
        String languageCode,
        BotState relatedBotState
) {
}
