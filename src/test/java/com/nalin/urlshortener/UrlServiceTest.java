package com.nalin.urlshortener;

import com.nalin.urlshortener.dto.ShortenResponse;
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
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

//
//@ExtendWith(MockitoExtension.class)
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
        when(urlRepository.existsByCode(anyString())).thenReturn(false); // Short url link not exist yet

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
    void getOriginalUrl_rejectsUnknownCode(){
        when(urlRepository.findByCode("Unknown1")).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> urlService.getOriginalUrl("Unknown1"));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

}