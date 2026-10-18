package com.nalin.urlshortener.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ShortenResponse(
        @JsonProperty("short_url")
        String shortUrl
) {}
