package ru.tinkoff.edu.java.scrapper.webclient.api;

import java.net.URI;
import java.util.List;

public interface BotClient {

    void sendUpdate(long id, URI url, String description, List<Long> tgChatIds);
}
