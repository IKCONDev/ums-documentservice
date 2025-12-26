package com.ikn.ums.exception;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ErrorResponse {

    private String message;
    private String errorCode;
    private HttpStatus status;
    private LocalDateTime timestamp;

    public ErrorResponse(String message, String errorCode, HttpStatus status) {
        this.message = message;
        this.errorCode = errorCode;
        this.status = status;
        this.timestamp = LocalDateTime.now(); // Automatically set timestamp
    }
}
