package ru.tinkoff.edu.java.scrapper.webclient.model;

import ru.tinkoff.edu.java.scrapper.model.request.LinkUpdateType;

import java.net.URI;
import java.util.List;

public record LinkUpdate(long id, URI url, LinkUpdateType updateType, List<Long> tgChatIds) {
}

