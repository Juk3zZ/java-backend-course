package ru.tinkoff.edu.java.scrapper.webclient.impl;

import org.springframework.web.reactive.function.client.WebClient;
import ru.tinkoff.edu.java.scrapper.model.request.LinkUpdateType;
import ru.tinkoff.edu.java.scrapper.webclient.api.BotClient;
import ru.tinkoff.edu.java.scrapper.webclient.model.LinkUpdate;

import java.net.URI;
import java.util.List;

public class HttpBotClient implements BotClient {

    private static final String BASE_URL = "https://localhost:9090/updates";

    private final String baseUrl;
    private final WebClient webClient;

    public HttpBotClient(WebClient webClient) {
        this.webClient = webClient;
        baseUrl = BASE_URL;
    }

    public HttpBotClient(String baseUrl, WebClient webClient) {
        this.baseUrl = baseUrl;
        this.webClient = webClient;
    }

    @Override
    public void sendUpdate(long id, URI url, LinkUpdateType updateType, List<Long> tgChatIds) {
        webClient.post()
                .uri(baseUrl)
                .bodyValue(new LinkUpdate(id, url, updateType, tgChatIds))
                .retrieve().bodyToMono(Void.class).subscribe();
    }
}
