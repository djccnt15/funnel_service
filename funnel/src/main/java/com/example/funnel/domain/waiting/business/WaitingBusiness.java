package com.example.funnel.domain.waiting.business;

import com.example.funnel.annotation.Business;
import com.example.funnel.domain.user.service.UserQueueService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.reactive.result.view.Rendering;
import reactor.core.publisher.Mono;

@Business
@RequiredArgsConstructor
public class WaitingBusiness {
    
    private final UserQueueService userQueueService;
    
    public Mono<Rendering> waitingRoomPage(String queue, Long userId, String redirectUrl) {
        return userQueueService.isAllowedUser(queue, userId)
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
