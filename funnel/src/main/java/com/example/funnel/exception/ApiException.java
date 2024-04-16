package com.example.funnel.exception;

import com.example.funnel.exceptionhandler.enums.StatusCode;
import lombok.Getter;

@Getter
public class ApiException extends RuntimeException {
    
    private final StatusCode statusCode;
    private final String detail;
    
    public ApiException(StatusCode statusCode) {
        this.statusCode = statusCode;
        this.detail = statusCode.getDetail();
    }
    
    public ApiException(StatusCode statusCode, Object ...args) {
        this.statusCode = statusCode;
        this.detail = statusCode.getDetail().formatted(args);
    }
}
