package com.example.funnel.domain.waiting.controller;

import com.example.funnel.domain.waiting.business.WaitingBusiness;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.reactive.result.view.Rendering;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.security.NoSuchAlgorithmException;

@Controller
@RequestMapping(path = "/waiting-room")
@RequiredArgsConstructor
public class WaitingController {
    
    private final WaitingBusiness waitingBusiness;
    
    @GetMapping
    public Mono<Rendering> waitingRoomPage(
        @RequestParam(name = "queue", defaultValue = "default") String queue,
        @RequestParam(name = "user_id") Long userId,
        @RequestParam(name = "redirect_url") String redirectUrl,
        ServerWebExchange exchange
    ) throws NoSuchAlgorithmException {
        return waitingBusiness.waitingRoomPage(queue, userId, redirectUrl, exchange);
    }
}
