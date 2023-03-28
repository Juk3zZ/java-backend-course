package ru.tinkoff.edu.java.scrapper.webclient.impl;

import org.springframework.web.reactive.function.client.WebClient;
import ru.tinkoff.edu.java.scrapper.webclient.api.StackOverflowClient;
import ru.tinkoff.edu.java.scrapper.webclient.model.StackOverflowItemApiResponse;
import ru.tinkoff.edu.java.scrapper.webclient.model.StackOverflowRootApiResponse;

public class HttpStackOverflowClient implements StackOverflowClient {

    private static final String BASE_URL = "https://api.stackexchange.com/2.3/questions/";
    private static final String STACKOVERFLOW_MANDATORY_REQUEST_PARAMS = "?order=desc&sort=activity&site=stackoverflow";

    private final String baseUrl;
    private final WebClient webClient;

    public HttpStackOverflowClient(WebClient webClient) {
        this.webClient = webClient;
        baseUrl = BASE_URL;
    }

    public HttpStackOverflowClient(String baseUrl, WebClient webClient) {
        this.baseUrl = baseUrl;
        this.webClient = webClient;
    }

    @Override
    public StackOverflowItemApiResponse fetchQuestion(long id) {
        var response = webClient.get()
                .uri(baseUrl + id + STACKOVERFLOW_MANDATORY_REQUEST_PARAMS)
                .retrieve().bodyToMono(StackOverflowRootApiResponse.class).block();
        return response.items().get(0);
    }
}