package com.trustbridge.Features.Auth;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntimeException(RuntimeException ex) {
        // If the message is "Email already in use", send a 400 instead of 500
        if (ex.getMessage().contains("Email already in use")) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
        return ResponseEntity.internalServerError().body("An unexpected error occurred");
    }

    @ExceptionHandler(org.springframework.security.authentication.BadCredentialsException.class)
    public ResponseEntity<String> handleBadCredentials(org.springframework.security.authentication.BadCredentialsException ex) {
        return ResponseEntity.status(401).body("Invalid email or password");
    }
}