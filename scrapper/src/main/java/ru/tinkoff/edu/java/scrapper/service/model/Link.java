package ru.tinkoff.edu.java.scrapper.service.model;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.Map;

public record Link(long id, URI link, OffsetDateTime updatedAt, Map<String, Object> updateInfo) {
}
