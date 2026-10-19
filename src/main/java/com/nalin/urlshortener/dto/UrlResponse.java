package com.nalin.urlshortener.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

public record UrlResponse(
        Long id,
        String code,
        @JsonProperty("short_url")
        String shortUrl,
        @JsonProperty("original_url")
        String originalUrl,
        @JsonProperty("created_at")
        Instant createdAt
) {}
