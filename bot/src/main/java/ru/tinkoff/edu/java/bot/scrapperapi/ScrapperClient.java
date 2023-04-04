package ru.tinkoff.edu.java.bot.scrapperapi;

import ru.tinkoff.edu.java.bot.scrapperapi.model.AllLinksApiResponse;
import ru.tinkoff.edu.java.bot.scrapperapi.model.LinkResponse;

public interface ScrapperClient {

    void registerChat(long chatId);

    void deleteChat(long chatId);

    AllLinksApiResponse getAllLinks(long tgChatId);

    LinkResponse addLink(long tgChatId, String link);

    LinkResponse deleteLink(long tgChatId, String link);
}
