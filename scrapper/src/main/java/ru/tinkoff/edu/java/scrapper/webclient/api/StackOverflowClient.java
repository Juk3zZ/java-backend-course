package ru.tinkoff.edu.java.scrapper.webclient.api;

import ru.tinkoff.edu.java.scrapper.webclient.model.StackOverflowItemApiResponse;

public interface StackOverflowClient {

    StackOverflowItemApiResponse fetchQuestion(long id);
}
