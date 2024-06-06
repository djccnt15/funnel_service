package com.example.funnel.domain.waiting.business;

import com.example.funnel.annotation.Business;
import com.example.funnel.domain.user.service.UserQueueService;
import com.example.funnel.domain.waiting.service.WaitingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.reactive.result.view.Rendering;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.security.NoSuchAlgorithmException;

@Business
@RequiredArgsConstructor
public class WaitingBusiness {
    
    private final UserQueueService userQueueService;
    private final WaitingService waitingService;
    
    public Mono<Rendering> waitingRoomPage(
        String queue, Long userId, String redirectUrl, ServerWebExchange exchange
    ) throws NoSuchAlgorithmException {
        var generatedToken = userQueueService.generateToken(queue, userId);
        var cookieToken = waitingService.getTokenFromCookie(queue, exchange);
        
        return userQueueService.isAllowedByToken(generatedToken, cookieToken)
            .filter(allowed -> allowed)
            .flatMap(allowed -> Mono.just(Rendering.redirectTo(redirectUrl).build()))
            .switchIfEmpty(
                userQueueService.registerWaitQueue(queue, userId)
                    .onErrorResume(ex -> userQueueService.getRank(queue, userId))
                    .map(rank -> Rendering.view("waiting-room.html")
                        .modelAttribute("number", rank)
                        .modelAttribute("userId", userId)
                        .modelAttribute("queue", queue)
                        .build()
                    )
            );
    }
}
