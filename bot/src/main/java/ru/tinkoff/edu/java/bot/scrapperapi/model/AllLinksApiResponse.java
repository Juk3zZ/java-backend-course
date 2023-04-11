package ru.tinkoff.edu.java.bot.scrapperapi.model;

import java.util.List;

public record AllLinksApiResponse(List<LinkResponse> links, int size) {
}
