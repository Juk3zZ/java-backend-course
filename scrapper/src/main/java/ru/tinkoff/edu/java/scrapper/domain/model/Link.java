package ru.tinkoff.edu.java.scrapper.domain.model;

import java.net.URI;
import java.sql.Timestamp;

public record Link(long id, URI link, Timestamp updatedAt) {
}
