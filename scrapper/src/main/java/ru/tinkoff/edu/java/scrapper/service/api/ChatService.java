package ru.tinkoff.edu.java.scrapper.service.api;

public interface ChatService {

    void register(long tgChatId, String username);

    void unregister(long tgChatId);
}
