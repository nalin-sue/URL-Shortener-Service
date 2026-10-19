package com.nalin.urlshortener.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@RestControllerAdvice
public class ApiExceptionHandler {
    // Our exceptions including 400, 401, 404, 409, 500
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiError> handleResponseStatus(ResponseStatusException ex) {
        int statusCode = ex.getStatusCode().value();
        return createErrorResponse(statusCode, ex.getReason());
    }

    // 400 from @Valid
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex) {
        FieldError error = ex.getBindingResult().getFieldError();
        String message = error != null ? error.getField() + " " + error.getDefaultMessage() : "Invalid input";
        return createErrorResponse(400, message);
    }

    // 400 for wrong path variable type or broken JSON body
    @ExceptionHandler({MethodArgumentTypeMismatchException.class, HttpMessageNotReadableException.class})
    public ResponseEntity<ApiError> handleUnreadableRequest() {
        return createErrorResponse(400, "Invalid request format");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnexpected(Exception ex) {
        // Errors handled by Spring with known status
        if (ex instanceof ErrorResponse errorResponse) {
            int statusCode = errorResponse.getStatusCode().value();
            return createErrorResponse(statusCode, HttpStatus.valueOf(statusCode).getReasonPhrase());
        }
        log.error("Unexpected error", ex);
        return createErrorResponse(500, "Something went wrong");
    }

    private ResponseEntity<ApiError> createErrorResponse(int statusCode, String message) {
        return ResponseEntity.status(statusCode).body(new ApiError(statusCode, message));
    }
}
