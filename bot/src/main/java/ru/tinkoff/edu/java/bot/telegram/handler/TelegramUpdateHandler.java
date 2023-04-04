package ru.tinkoff.edu.java.bot.telegram.handler;

import org.telegram.telegrambots.meta.api.objects.Update;

public interface TelegramUpdateHandler<T> {

    T handle(Update update);
}
