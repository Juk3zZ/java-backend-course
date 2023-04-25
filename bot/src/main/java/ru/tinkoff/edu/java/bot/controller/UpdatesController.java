package ru.tinkoff.edu.java.bot.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.tinkoff.edu.java.bot.model.LinkUpdate;
import ru.tinkoff.edu.java.bot.telegram.TelegramBot;

@RestController
@RequestMapping("/updates")
public class UpdatesController {

    private final TelegramBot telegramBot;

    public UpdatesController(TelegramBot telegramBot) {
        this.telegramBot = telegramBot;
    }

    @PostMapping
    public void sendUpdate(@RequestBody LinkUpdate update) {
        telegramBot.notifyAboutLinkUpdate(update.url().toString(), update.description(), update.tgChatIds());
    }
}
