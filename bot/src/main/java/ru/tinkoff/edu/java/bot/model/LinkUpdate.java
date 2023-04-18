package ru.tinkoff.edu.java.bot.model;

import java.net.URI;
import java.util.List;

public record LinkUpdate(long id, URI url, LinkUpdateType updateType, List<Long> tgChatIds) {
}
