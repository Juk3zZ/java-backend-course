package ru.tinkoff.edu.java.scrapper.controller;

import org.springframework.web.bind.annotation.*;
import ru.tinkoff.edu.java.common.exception.IncorrectRequestParamsException;

import java.util.HashSet;
import java.util.Set;

@RestController
@RequestMapping("/tg-chat")
public class TgChatController {

    private final Set<Long> tgIds = new HashSet<>();

    @PostMapping("/{id}")
    public void registerChat(@PathVariable long id) {
        if (!tgIds.add(id))
            throw new IncorrectRequestParamsException(String.format("Telegram chat with id=[%d] already exists", id));
    }

    @DeleteMapping("/{id}")
    public void deleteChat(@PathVariable long id) {
        if (!tgIds.remove(id))
            throw new IncorrectRequestParamsException(String.format("Telegram chat with id=[%d] doesn't exist", id));
    }

}
