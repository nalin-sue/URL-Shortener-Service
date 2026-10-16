package com.nalin.urlshortener.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest (
    @NotBlank
    @Email
    String email,

    @Schema(example = "demo1234")
    @NotBlank
    @Size(min = 8, max = 72)
    String password
){}
