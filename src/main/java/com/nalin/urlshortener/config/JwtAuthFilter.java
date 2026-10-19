package com.nalin.urlshortener.config;

import com.nalin.urlshortener.exception.ApiError;
import com.nalin.urlshortener.service.JwtService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.List;

@Slf4j
public class JwtAuthFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final ObjectMapper objectMapper; // Turn ApiError into JSON

    @Autowired
    public JwtAuthFilter(JwtService jwtService, ObjectMapper objectMapper) {
        this.jwtService = jwtService;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);

        // Guest user, no login token
        if (header == null) {
            filterChain.doFilter(request, response);
            return;
        }

        // Logged-in user with token
        if (!header.startsWith("Bearer ")) {
            writeUnauthorized(response, "Authorization header must be: Bearer <token>");
            return;
        }

        Long userId;

        try {
            userId = jwtService.getUserId(header.substring(7)); // Pass text after "Bearer "
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("Invalid JWT ({})", e.getMessage());
            writeUnauthorized(response, "Invalid or expired token");
            return;
        }

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(userId, null, List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);
        filterChain.doFilter(request, response);
    }

    private void writeUnauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        objectMapper.writeValue(response.getWriter(), new ApiError(401, message));
    }
}
