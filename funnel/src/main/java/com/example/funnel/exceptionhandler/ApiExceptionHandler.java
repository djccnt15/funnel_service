package com.example.funnel.exceptionhandler;

import com.example.funnel.exception.ApiException;
import com.example.funnel.model.Body;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import reactor.core.publisher.Mono;

@RestControllerAdvice
public class ApiExceptionHandler {
    
    @ExceptionHandler(value = ApiException.class)
    Mono<ResponseEntity<Body>> apiExceptionHandler(ApiException ex) {
        var body = Body.builder()
            .code(ex.getStatusCode().getCode())
            .detail(ex.getDetail())
            .build();
        
        return Mono.just(ResponseEntity
            .status(ex.getStatusCode().getHttpStatus())
            .body(body)
        );
    }
}
