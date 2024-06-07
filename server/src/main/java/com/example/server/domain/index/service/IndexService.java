package com.example.server.domain.index.service;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Arrays;

@Service
public class IndexService {
    
    public String getTokenFromCookie(
        final String queue,
        final HttpServletRequest servletRequest
    ) {
        var cookies = servletRequest.getCookies();
        var tokenCookieName = "user-queue-%s-token".formatted(queue);
        
        if (cookies != null) {
            var cookie = Arrays.stream(cookies)
                .filter(i -> i.getName().equalsIgnoreCase(tokenCookieName)).findFirst();
            return cookie.orElse(new jakarta.servlet.http.Cookie(tokenCookieName, "")).getValue();
        }
        return "";
    }

    public URI createAvailableCheckUri(
        final String queue,
        final Long userId,
        final String token
    ) {
        return UriComponentsBuilder
            .fromUriString("http://localhost:8080")
            .path("api/user/queue/available")
            .queryParam("queue", queue)
            .queryParam("user_id", userId)
            .queryParam("token", token)
            .encode()
            .build()
            .toUri();
    }
}
