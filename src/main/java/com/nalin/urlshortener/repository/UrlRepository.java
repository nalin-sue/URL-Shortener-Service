package com.nalin.urlshortener.repository;

import com.nalin.urlshortener.entity.Url;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UrlRepository extends JpaRepository<Url, Long> {
    boolean existsByCode(String code);
    Optional<Url> findByCode(String code);
    List<Url> findByUserIdOrderByCreatedAtDesc(Long userId); // Newest first
}
