package com.nalin.urlshortener.service;

import com.nalin.urlshortener.dto.LoginResponse;
import com.nalin.urlshortener.dto.LoginRequest;
import com.nalin.urlshortener.dto.RegisterRequest;
import com.nalin.urlshortener.entity.User;
import com.nalin.urlshortener.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Autowired
    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public void register(RegisterRequest request){
        String email = prepareEmail(request.email());

        if(userRepository.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already used");
        }
        if (request.password().getBytes(StandardCharsets.UTF_8).length > 72){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password is too long");
        }

        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        userRepository.save(user);
    }

    public LoginResponse login(LoginRequest request){
        String email = prepareEmail(request.email());
        User user = userRepository.findByEmail(email)
                .filter(dbUser -> passwordEncoder.matches(request.password(), dbUser.getPasswordHash()))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "Wrong email or password"
                ));
        return new LoginResponse(jwtService.generateToken(user.getId()));
    }

    private String prepareEmail(String input){
        return input.trim().toLowerCase();
    }
}
