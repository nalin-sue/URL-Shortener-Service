package com.nalin.urlshortener.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class ApiExceptionHandler {
    // 409, 401, 404
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiError> handleResponseStatus(ResponseStatusException ex) {
        int statusCode = ex.getStatusCode().value();
        return ResponseEntity.status(statusCode).body(new ApiError(statusCode, ex.getReason()));
    }

    // 400 from @Valid
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex) {
        FieldError error = ex.getBindingResult().getFieldError();
        String message = error != null ? error.getField() + " " + error.getDefaultMessage() : "Invalid input";
        return ResponseEntity.badRequest().body(new ApiError(400, message));
    }
}
