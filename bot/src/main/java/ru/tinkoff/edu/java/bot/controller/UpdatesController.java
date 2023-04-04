package ru.tinkoff.edu.java.bot.controller;

import org.springframework.web.bind.annotation.*;
import ru.tinkoff.edu.java.bot.model.LinkUpdate;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/updates")
public class UpdatesController {

    private final List<LinkUpdate> linkUpdates = new ArrayList<>();

    @PostMapping
    public void sendUpdate(@RequestBody LinkUpdate update) {
        linkUpdates.add(update);
    }
}
