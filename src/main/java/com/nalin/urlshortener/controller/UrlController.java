package com.nalin.urlshortener.controller;

import com.nalin.urlshortener.dto.ShortenRequest;
import com.nalin.urlshortener.dto.ShortenResponse;
import com.nalin.urlshortener.service.UrlService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class UrlController {
    private final UrlService urlService;

    @Autowired
    public UrlController(UrlService urlService) {
        this.urlService = urlService;
    }

    @PostMapping("/shorten")
    @ResponseStatus(HttpStatus.CREATED)
    public ShortenResponse shortenUrl(@Valid @RequestBody ShortenRequest request) {
        return urlService.shortenUrl(request.originalUrl(), null); // Guest, TODO: Add user
    }
}
