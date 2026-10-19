package com.nalin.urlshortener;

import com.nalin.urlshortener.dto.ShortenResponse;
import com.nalin.urlshortener.dto.UrlResponse;
import com.nalin.urlshortener.entity.Url;
import com.nalin.urlshortener.repository.UrlRepository;
import com.nalin.urlshortener.service.UrlService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UrlServiceTest {
    @Mock
    private UrlRepository urlRepository;
    private UrlService urlService;

    @BeforeEach
    void setUp() {
        urlService = new UrlService(urlRepository, "https://short.example");
    }

    @Test
    void shorten_passesAndSavesUrlForGuest() {
        when(urlRepository.existsByCode(anyString())).thenReturn(false); // Short URL not exist yet

        ShortenResponse response = urlService.shortenUrl("https://example.com/some/very/long/page", null);

        ArgumentCaptor<Url> captor = ArgumentCaptor.forClass(Url.class);
        verify(urlRepository).save(captor.capture());
        Url savedUrl = captor.getValue();

        assertEquals("https://example.com/some/very/long/page", savedUrl.getOriginalUrl());
        assertNull(savedUrl.getUserId());
        assertEquals(6, savedUrl.getCode().length());
        assertEquals("https://short.example/" + savedUrl.getCode(), response.shortUrl());
    }

    @Test
    void shorten_passesAndSavesUserIdWhenLoggedIn() {
        when(urlRepository.existsByCode(anyString())).thenReturn(false);

        urlService.shortenUrl("https://example.com/some/very/long/page", 42L);

        ArgumentCaptor<Url> captor = ArgumentCaptor.forClass(Url.class);
        verify(urlRepository).save(captor.capture());
        assertEquals(42L, captor.getValue().getUserId());
    }

    @Test
    void shorten_rejectsNonHttpUrl() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> urlService.shortenUrl("javascript:alert(1)", null));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        verify(urlRepository, never()).save(any());
    }

    @Test
    void shorten_rejectsAppDomain() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> urlService.shortenUrl("https://short.example", null));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertEquals("Cannot shorten a link from this site", ex.getReason());
        verify(urlRepository, never()).save(any());
    }

    @Test
    void shorten_retriesWhenCodeTaken() {
        // First generated code is taken, second is free
        when(urlRepository.existsByCode(anyString())).thenReturn(true, false);

        urlService.shortenUrl("https://example.com/some/very/long/page", null);

        verify(urlRepository, times(2)).existsByCode(anyString());
        verify(urlRepository).save(any());
    }

    @Test
    void getOriginalUrl_passesAndReturnsWhenCodeExists() {
        Url url = new Url("aB1cDE2", "https://example.com/some/very/long/page", null);
        when(urlRepository.findByCode("aB1cDE2")).thenReturn(Optional.of(url));

        String result = urlService.getOriginalUrl("aB1cDE2");

        assertEquals("https://example.com/some/very/long/page", result);
    }

    @Test
    void getOriginalUrl_rejectsUnknownCode() {
        when(urlRepository.findByCode("Unknown1")).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> urlService.getOriginalUrl("Unknown1"));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void getUserUrls_passesAndReturnsUserUrls() {
        Instant newer = Instant.parse("2026-10-19T10:00:00Z");
        Instant older = Instant.parse("2026-10-18T10:00:00Z");

        Url newerUrl = createSavedUrl(2L, "dEf456", "https://example.com/newer/page", newer);
        Url olderUrl = createSavedUrl(1L, "aBc123", "https://example.com/older/page", older);
        when(urlRepository.findByUserIdOrderByCreatedAtDesc(42L)).thenReturn(List.of(newerUrl, olderUrl));

        List<UrlResponse> result = urlService.getUserUrls(42L);
        List<UrlResponse> expected = List.of(
                new UrlResponse(2L, "dEf456", "https://short.example/dEf456", "https://example.com/newer/page", newer),
                new UrlResponse(1L, "aBc123", "https://short.example/aBc123", "https://example.com/older/page", older)
        );
        assertEquals(expected, result);
    }

    @Test
    void getUserUrls_passedAndReturnsEmptyList() { // A user with no short URLs gets an empty list, not an error
        when(urlRepository.findByUserIdOrderByCreatedAtDesc(42L)).thenReturn(List.of());

        List<UrlResponse> result = urlService.getUserUrls(42L);

        assertTrue(result.isEmpty());
    }

    @Test
    void deleteUrl_passesAndDeletesUrlOwnedByUser() {
        Url url= createSavedUrl(5L, "aBc123", "https://example.com/some/very/long/page", Instant.now());
        when(urlRepository.findByIdAndUserId(5L,42L)).thenReturn(Optional.of(url));

        urlService.deleteUrl(5L, 42L);

        verify(urlRepository).delete(url);
    }

    @Test
    void deleteUrl_rejectsUrlNotOwnedByUser(){
        when(urlRepository.findByIdAndUserId(5L, 42L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> urlService.deleteUrl(5L, 42L));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        verify(urlRepository, never()).delete(any());
    }

    private Url createSavedUrl(Long id, String code, String originalUrl, Instant createdAt) {
        Url url = new Url(code, originalUrl, 42L);
        // id and createdAt normally set by database, so set them manually for testing
        ReflectionTestUtils.setField(url, "id", id);
        ReflectionTestUtils.setField(url, "createdAt", createdAt);
        return url;
    }
}