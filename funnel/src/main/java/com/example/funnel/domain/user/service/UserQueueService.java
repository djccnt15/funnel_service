package com.example.funnel.domain.user.service;

import com.example.funnel.exception.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Objects;

import static com.example.funnel.exceptionhandler.enums.StatusCode.QUEUE_ALREADY_REGISTERED_USER;

@Service
@RequiredArgsConstructor
public class UserQueueService {
    
    private final ReactiveRedisTemplate<String, String> reactiveRedisTemplate;
    private final String USER_QUEUE_WAIT_KEY = "users:queue:%s:wait";
    private final String USER_QUEUE_PROCEED_KEY = "users:queue:%s:proceed";
    
    public Mono<Long> registerWaitQueue(
        final String queue,
        final Long userId
    ) {
        var timeStamp = Instant.now().getEpochSecond();
        
        return reactiveRedisTemplate.opsForZSet().add(USER_QUEUE_WAIT_KEY.formatted(queue), userId.toString(), timeStamp)
            .filter(i -> i)  // 정상 add 시 true 반환, 이미 있을 경우 false 반환
            .switchIfEmpty(Mono.error(new ApiException(QUEUE_ALREADY_REGISTERED_USER)))
            .flatMap(i -> reactiveRedisTemplate.opsForZSet().rank(USER_QUEUE_WAIT_KEY.formatted(queue), userId.toString()))
            .map(i -> i >= 0 ? i + 1 : i);
    }
    
    // 접근 허용
    public Mono<Long> allowUser(
        final String queue,
        final Long availableCount
    ) {
        var timeStamp = Instant.now().getEpochSecond();
        
        return reactiveRedisTemplate.opsForZSet()
            .popMin(USER_QUEUE_WAIT_KEY.formatted(queue), availableCount)
            .flatMap(
                member -> reactiveRedisTemplate.opsForZSet()
                    .add(USER_QUEUE_PROCEED_KEY.formatted(queue), Objects.requireNonNull(member.getValue()), timeStamp)
            )
            .count();
    }
    
    // 접근이 가능한 상태인지 조회
    public Mono<Boolean> isAllowedUser(
        final String queue,
        final Long userId
    ) {
        return reactiveRedisTemplate.opsForZSet()
            .rank(USER_QUEUE_PROCEED_KEY.formatted(queue), userId.toString())
            .defaultIfEmpty(-1L)
            .map(rank -> rank >= 0);
    }
}
