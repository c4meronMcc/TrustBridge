package com.trustbridge.Features.Auth;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles RuntimeException instances and returns an appropriate HTTP response.
     * If the exception message contains "Email already in use", a 400 Bad Request response
     * is returned with the exception message as the body. Otherwise, a 500 Internal Server Error
     * response is returned with a generic error message.
     *
     * @param ex the RuntimeException to be handled
     * @return a ResponseEntity containing the appropriate HTTP status and message based on the exception
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntimeException(RuntimeException ex) {
        // If the message is "Email already in use", send a 400 instead of 500
        if (ex.getMessage().contains("Email already in use")) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
        return ResponseEntity.internalServerError().body("An unexpected error occurred");
    }

    /**
     * Handles exceptions of type BadCredentialsException and returns an appropriate HTTP response.
     * This typically occurs when the user's authentication details, such as email or password, are invalid.
     *
     * @param ex the BadCredentialsException to be handled, typically triggered during authentication failures
     * @return a ResponseEntity with an HTTP 401 Unauthorized status and a message indicating invalid credentials
     */
    @ExceptionHandler(org.springframework.security.authentication.BadCredentialsException.class)
    public ResponseEntity<String> handleBadCredentials(org.springframework.security.authentication.BadCredentialsException ex) {
        return ResponseEntity.status(401).body("Invalid email or password");
    }
}