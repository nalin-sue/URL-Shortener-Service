package com.nalin.urlshortener.controller;

import com.nalin.urlshortener.dto.ShortenRequest;
import com.nalin.urlshortener.dto.ShortenResponse;
import com.nalin.urlshortener.dto.UrlResponse;
import com.nalin.urlshortener.service.UrlService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public ShortenResponse shortenUrl(@Valid @RequestBody ShortenRequest request, @AuthenticationPrincipal Long userId) {
        return urlService.shortenUrl(request.originalUrl(), userId); // Guest's userId = null
    }

    @GetMapping("/urls")
    public List<UrlResponse> getLoggedInUserUrls(@AuthenticationPrincipal Long userId) {
        return urlService.getUserUrls(userId);
    }
}
