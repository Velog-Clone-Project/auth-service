package com.example.authservice.exception.handler;

import com.example.authservice.dto.ApiResponse;
import com.example.authservice.exception.base.BaseCustomException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BaseCustomException.class)
    public ResponseEntity<Map<String, String>> handleCustomException(BaseCustomException e) {

        Map<String, String> body = Map.of("message", e.getMessage());

        return ResponseEntity
                .status(e.getStatusCode())
                .body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleUnexpectedException(Exception e) {

        Map<String, String> body = Map.of("message", e.getMessage());

        return ResponseEntity
                .status(500)
                .body(body);
    }
}