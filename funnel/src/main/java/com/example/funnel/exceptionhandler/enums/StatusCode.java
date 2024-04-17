package com.example.funnel.exceptionhandler.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
public enum StatusCode {
    
    QUEUE_ALREADY_REGISTERED_USER(HttpStatus.CONFLICT, "UQ-0001", "already registered in queue");

    private final HttpStatus httpStatus;
    private final String code;
    private final String detail;
}
