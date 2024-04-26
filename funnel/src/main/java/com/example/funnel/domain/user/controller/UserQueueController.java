package com.example.funnel.domain.user.controller;

import com.example.funnel.domain.user.business.UserQueueBusiness;
import com.example.funnel.domain.user.model.AllowUserResponse;
import com.example.funnel.domain.user.model.AllowedUserResponse;
import com.example.funnel.domain.user.model.RankNumberResponse;
import com.example.funnel.domain.user.model.RegisterUserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.security.NoSuchAlgorithmException;

@RestController
@RequestMapping(path = "api/user/queue")
@RequiredArgsConstructor
public class UserQueueController {
    
    private final UserQueueBusiness userQueueBusiness;
    
    @PostMapping
    public Mono<RegisterUserResponse> registerUser(
        @RequestParam(name = "queue", defaultValue = "default") String queue,
        @RequestParam(name = "user_id") Long userId
    ) {
        return userQueueBusiness.registerUser(queue, userId);
    }
    
    @PostMapping(path = "/allow")
    public Mono<AllowUserResponse> allowUser(
        @RequestParam(name = "queue", defaultValue = "default") String queue,
        @RequestParam(name = "count") Long count
    ) {
        return userQueueBusiness.allowUser(queue, count);
    }
    
    @GetMapping(path = "/available")
    public Mono<AllowedUserResponse> isAllowedUser(
        @RequestParam(name = "queue", defaultValue = "default") String queue,
        @RequestParam(name = "user_id") Long userId,
        @RequestParam(name = "token") String token
    ) throws NoSuchAlgorithmException {
        return userQueueBusiness.isAllowedByToken(queue, userId, token);
    }
    
    @GetMapping(path = "/rank")
    public Mono<RankNumberResponse> getUserRank(
        @RequestParam(name = "queue", defaultValue = "default") String queue,
        @RequestParam(name = "user_id") Long userId
    ) {
        return userQueueBusiness.getRank(queue, userId);
    }
    
    @GetMapping(path = "/touch")
    public Mono<String> touch(
        @RequestParam(name = "queue", defaultValue = "default") String queue,
        @RequestParam(name = "user_id") Long userId,
        ServerWebExchange exchange
    ) throws NoSuchAlgorithmException {
        return userQueueBusiness.generateToken(queue, userId, exchange);
    }
}
