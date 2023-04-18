package ru.tinkoff.edu.java.scrapper.domain.model;

import java.net.URI;
import java.sql.Timestamp;
import java.util.Map;

public record Link(long id, URI link, Timestamp updatedAt, Map<String, Object> updateInfo) {
}
