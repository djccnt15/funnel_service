package com.example.funnel.domain.user.business;

import com.example.funnel.annotation.Business;
import com.example.funnel.domain.user.converter.UserQueueConverter;
import com.example.funnel.domain.user.model.AllowUserResponse;
import com.example.funnel.domain.user.model.AllowedUserResponse;
import com.example.funnel.domain.user.model.RankNumberResponse;
import com.example.funnel.domain.user.model.RegisterUserResponse;
import com.example.funnel.domain.user.service.UserQueueService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.security.NoSuchAlgorithmException;
import java.time.Duration;

@Slf4j
@Business
@RequiredArgsConstructor
public class UserQueueBusiness {
    
    private final UserQueueService userQueueService;
    private final UserQueueConverter userQueueConverter;
    
    @Value("${scheduler.enabled}")
    private final Boolean scheduling = false;
    
    public Mono<RegisterUserResponse> registerUser(String queue, Long userId) {
        return userQueueService.registerWaitQueue(queue, userId)
            .map(userQueueConverter::toRegisterUserResponse);
    }
    
    public Mono<AllowUserResponse> allowUser(String queue, Long count) {
        return userQueueService.allowUser(queue, count)
            .map(allowed -> AllowUserResponse.builder()
                .requestCount(count)
                .allowedCount(allowed)
                .build());
    }
    
    public Mono<AllowedUserResponse> isAllowedUser(String queue, Long userId) {
        return userQueueService.isAllowedUser(queue, userId)
            .map(allowed -> AllowedUserResponse.builder()
                .allowed(allowed).build());
    }
    
    public Mono<AllowedUserResponse> isAllowedByToken(
        String queue, Long userId, String token
    ) throws NoSuchAlgorithmException {
        var generatedToken = userQueueService.generateToken(queue, userId);
        
        return userQueueService.isAllowedByToken(generatedToken, token)
            .map(allowed -> AllowedUserResponse.builder()
                .allowed(allowed).build()
            );
    }
    
    public Mono<RankNumberResponse> getRank(String queue, Long userId) {
        return userQueueService.getRank(queue, userId)
            .map(rank -> RankNumberResponse.builder().rank(rank).build());
    }
    
    // 서버 시작 5초 이후 3초 주기로 스케쥴 실행
    @Scheduled(initialDelay = 5000, fixedDelay = 3000)
    public void scheduleAllowUser() {
        if (!scheduling) {
            log.info("allow user queue schedule passed");
            return;
        }
        
        log.info("allow user queue schedule called");
        
        var maxAllowUser = 3L;
        userQueueService.scheduleAllowUser(maxAllowUser);
    }
    
    public Mono<String> generateToken(
        String queue, Long userId, ServerWebExchange exchange
    ) throws NoSuchAlgorithmException {
        var token = userQueueService.generateToken(queue, userId);
        
        return Mono.defer(() -> token)
            .map(t -> {
                exchange.getResponse().addCookie(
                    ResponseCookie
                        .from("user-queue-%s-token".formatted(queue), t)
                        .maxAge(Duration.ofSeconds(300))
                        .path("/")
                        .build()
                );
                return t;
            });
    }
}
