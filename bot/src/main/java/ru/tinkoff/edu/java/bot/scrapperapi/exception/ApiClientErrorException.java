package ru.tinkoff.edu.java.bot.scrapperapi.exception;

import ru.tinkoff.edu.java.common.model.ApiErrorResponse;

public class ApiClientErrorException extends RuntimeException {

    private final ApiErrorResponse apiErrorResponse;

    public ApiClientErrorException(ApiErrorResponse apiErrorResponse) {
        super("Incorrect request for Scrapper");
        this.apiErrorResponse = apiErrorResponse;
    }

    public ApiErrorResponse getApiErrorResponse() {
        return apiErrorResponse;
    }
}
