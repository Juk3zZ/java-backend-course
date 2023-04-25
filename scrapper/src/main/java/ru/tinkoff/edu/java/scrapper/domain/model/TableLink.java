package ru.tinkoff.edu.java.scrapper.domain.model;

import java.sql.Timestamp;
import java.util.Map;

public record TableLink(long id, String link, Timestamp updatedAt, Map<String, Object> updateInfo) {
}
