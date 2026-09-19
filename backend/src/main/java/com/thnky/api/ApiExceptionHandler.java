package com.thnky.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.thnky.api.dto.ErrorResponse;
import com.thnky.challenge.ChallengeNotFoundException;
import com.thnky.challenge.ChallengeUnavailableException;
import com.thnky.domain.InvalidChallengeParamsException;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(InvalidChallengeParamsException.class)
    public ResponseEntity<ErrorResponse> onInvalidParams(InvalidChallengeParamsException e) {
        return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
    }

    @ExceptionHandler({ChallengeNotFoundException.class, ChallengeUnavailableException.class})
    public ResponseEntity<ErrorResponse> onNotFound(RuntimeException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(e.getMessage()));
    }
}
