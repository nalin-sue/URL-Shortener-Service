package com.nalin.urlshortener.controller;

import com.nalin.urlshortener.service.UrlService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
public class RedirectController {
    private final UrlService urlService;

    @Autowired
    public RedirectController(UrlService urlService) {
        this.urlService = urlService;
    }

    // Get value after "our-app-domain/" as the code
    // Paths that can't be our code are rejected before any DB lookup
    @GetMapping("/{code:[0-9A-Za-z]+}")
    public ResponseEntity<Void> redirect(@PathVariable String code) {
        String originalUrl = urlService.getOriginalUrl(code);
        return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(originalUrl)).build();
    }
}
