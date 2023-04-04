package ru.tinkoff.edu.java.scrapper.webclient.api;

import ru.tinkoff.edu.java.linkparser.model.UserAndRepo;
import ru.tinkoff.edu.java.scrapper.webclient.model.GitHubApiResponse;

public interface GitHubClient {

    GitHubApiResponse fetchRepo(UserAndRepo userAndRepo);
}
