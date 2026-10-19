package com.nalin.urlshortener.service;

import com.nalin.urlshortener.dto.ShortenResponse;
import com.nalin.urlshortener.dto.UrlResponse;
import com.nalin.urlshortener.entity.Url;
import com.nalin.urlshortener.repository.UrlRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.net.URISyntaxException;
import java.security.SecureRandom;
import java.util.List;

@Slf4j
@Service
public class UrlService {
    private static final String CODE_CHARACTERS = "23456789abcdefghijkmnopqrstuvwxyzABCDEFGHJKLMNPQRSTUVWXYZ";
    private static final int CODE_LENGTH = 6;
    private static final int MAX_ATTEMPTS = 5;

    private final UrlRepository urlRepository;
    private final String shortBaseUrl;
    private final String appHost;
    private final SecureRandom random = new SecureRandom();

    @Autowired
    public UrlService(UrlRepository urlRepository, @Value("${app.base-url}") String shortBaseUrl) {
        this.urlRepository = urlRepository;
        this.shortBaseUrl = shortBaseUrl.endsWith("/") ? shortBaseUrl.substring(0, shortBaseUrl.length() - 1) : shortBaseUrl;
        this.appHost = prepareHost(URI.create(shortBaseUrl).getHost());
    }

    public ShortenResponse shortenUrl(String originalUrl, Long userId) {
        String url = originalUrl.trim();
        validateUrl(url);
        String code = createUniqueCode();
        urlRepository.save(new Url(code, url, userId));
        log.info("Created code '{}' for user '{}'", code, userId != null ? userId : "guest");
        return new ShortenResponse(shortBaseUrl + "/" + code);
    }

    public String getOriginalUrl(String code) {
        return urlRepository.findByCode(code).map(Url::getOriginalUrl).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Short link not found"));
    }

    public List<UrlResponse> getUserUrls(Long userId) {
        return urlRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(url -> new UrlResponse(
                        url.getId(),
                        url.getCode(),
                        shortBaseUrl + "/" + url.getCode(),
                        url.getOriginalUrl(),
                        url.getCreatedAt())
                ).toList();
    }

    private ResponseStatusException createBadRequestException(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }

    private void validateUrl(String url) {
        URI uri;
        try {
            uri = new URI(url);
        } catch (URISyntaxException e) {
            throw createBadRequestException("Invalid URL");
        }

        String scheme = uri.getScheme();
        if (scheme == null || !(scheme.equalsIgnoreCase("http") || scheme.equalsIgnoreCase("https"))) {
            throw createBadRequestException("Only http and https link are allowed");
        }

        String host = uri.getHost();
        if (host == null || !host.contains(".")) {
            throw createBadRequestException("URL must have a valid domain, like example.com");
        }

        String preparedHost = prepareHost(host);
        if (preparedHost.equals(appHost) || preparedHost.endsWith("." + appHost)) {
            throw createBadRequestException("Cannot shorten a link from this site");
        }
    }

    private String createUniqueCode() {
        for (int i = 0; i < MAX_ATTEMPTS; i++) {
            String code = createRandomCode();
            if (!urlRepository.existsByCode(code)) {
                return code;
            }
        }
        throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not generate a unique code");
    }

    private String createRandomCode() {
        StringBuilder builder = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            builder.append(CODE_CHARACTERS.charAt(random.nextInt(CODE_CHARACTERS.length())));
        }
        return builder.toString();
    }

    private String prepareHost(String host) {
        String lowercaseHost = host.toLowerCase();
        return lowercaseHost.startsWith("www.") ? lowercaseHost.substring(4) : lowercaseHost;
    }
}
