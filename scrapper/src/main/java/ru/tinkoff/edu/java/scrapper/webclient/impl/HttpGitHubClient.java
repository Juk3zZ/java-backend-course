package ru.tinkoff.edu.java.scrapper.webclient.impl;

import org.springframework.web.reactive.function.client.WebClient;
import ru.tinkoff.edu.java.linkparser.model.UserAndRepo;
import ru.tinkoff.edu.java.scrapper.webclient.api.GitHubClient;
import ru.tinkoff.edu.java.scrapper.webclient.model.GitHubApiResponse;

public class HttpGitHubClient implements GitHubClient {

    private static final String BASE_URL = "https://api.github.com/repos";
    private static final String USER_PATH_VAR = "{user}";
    private static final String REPO_PATH_VAR = "{repo}";

    private final String baseUrl;
    private final WebClient webClient;

    public HttpGitHubClient(WebClient webClient) {
        this.webClient = webClient;
        baseUrl = BASE_URL;
    }

    public HttpGitHubClient(String baseUrl, WebClient webClient) {
        this.baseUrl = baseUrl;
        this.webClient = webClient;
    }

    @Override
    public GitHubApiResponse fetchRepo(UserAndRepo userAndRepo) {
        return webClient
                .get()
                .uri(
                        baseUrl,
                        uriBuilder -> uriBuilder.pathSegment(USER_PATH_VAR, REPO_PATH_VAR)
                                .build(userAndRepo.user(), userAndRepo.repository())
                )
                .retrieve().bodyToMono(GitHubApiResponse.class).block();
    }
}