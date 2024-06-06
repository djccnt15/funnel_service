package com.example.funnel.domain.waiting.service;

import org.springframework.stereotype.Service;
import org.springframework.web.server.ServerWebExchange;

@Service
public class WaitingService {
    
    public String getTokenFromCookie(
        final String queue,
        final ServerWebExchange exchange
    ) {
        var key = "user-queue-%s-token".formatted(queue);
        var cookie = exchange.getRequest().getCookies().getFirst(key);
        return (cookie == null) ? "" : cookie.getValue();
    }
}
