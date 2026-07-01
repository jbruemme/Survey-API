package org.example.ser421lab6.controller;

import org.example.ser421lab6.dto.ErrorResponse;
import org.example.ser421lab6.exception.InvalidSurveyVisibilityException;
import org.example.ser421lab6.exception.SurveyNotFoundException;
import org.example.ser421lab6.exception.UnauthorizedAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(MethodArgumentNotValidException e) {
        String message = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(error -> error.getDefaultMessage())
                .orElse("Validation failed.");

        return ResponseEntity.badRequest().body(new ErrorResponse(message));
    }

    @ExceptionHandler(InvalidSurveyVisibilityException.class)
    public ResponseEntity<ErrorResponse> handleInvalidSurveyVisibility(InvalidSurveyVisibilityException e) {
        return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
    }

    @ExceptionHandler(SurveyNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleSurveyNotFound(SurveyNotFoundException e) {
        return ResponseEntity.status(404).body(new ErrorResponse(e.getMessage()));
    }

    @ExceptionHandler(UnauthorizedAccessException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorizedSurveyAccess(UnauthorizedAccessException e) {
        return ResponseEntity.status(403).body(new ErrorResponse(e.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
    }

}