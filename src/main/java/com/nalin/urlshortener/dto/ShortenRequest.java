package com.nalin.urlshortener.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ShortenRequest(
    @NotBlank
    @Size(max = 2048)
    @JsonProperty("original_url")
    String originalUrl
){}
