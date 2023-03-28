package ru.tinkoff.edu.java.scrapper.webclient.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;

public record GitHubApiResponse(
        @JsonProperty("id") long id,
        @JsonProperty("clone_url") String cloneUrl,
        @JsonProperty("name") String repoName,
        @JsonProperty("private") boolean isPrivate,
        @JsonProperty("created_at") OffsetDateTime createdAt,
        @JsonProperty("updated_at") OffsetDateTime updatedAt,
        @JsonProperty("pushed_at") OffsetDateTime pushedAt
) {
}