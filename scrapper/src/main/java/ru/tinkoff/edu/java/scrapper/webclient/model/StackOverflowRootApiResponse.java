package ru.tinkoff.edu.java.scrapper.webclient.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record StackOverflowRootApiResponse(
        @JsonProperty("items") List<StackOverflowItemApiResponse> items
) {
}