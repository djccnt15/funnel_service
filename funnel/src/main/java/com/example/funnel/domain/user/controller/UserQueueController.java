package com.example.funnel.domain.user.controller;

import com.example.funnel.domain.user.business.UserQueueBusiness;
import com.example.funnel.domain.user.model.AllowUserResponse;
import com.example.funnel.domain.user.model.AllowedUserResponse;
import com.example.funnel.domain.user.model.RegisterUserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

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
        @RequestParam(name = "user_id") Long count
    ) {
        return userQueueBusiness.isAllowedUser(queue, count)            ;
    }
}
