package com.example.funnel.domain.user.service;

import com.example.funnel.exception.ApiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuples;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Objects;

import static com.example.funnel.exceptionhandler.enums.StatusCode.QUEUE_ALREADY_REGISTERED_USER;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserQueueService {
    
    private final ReactiveRedisTemplate<String, String> reactiveRedisTemplate;
    private final String USER_QUEUE_WAIT_KEY = "users:queue:%s:wait";
    private final String USER_QUEUE_WAIT_KEY_FOR_SCAN = "users:queue:*:wait";
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
    
    public Mono<Boolean> isAllowedByToken(
        final Mono<String> generatedToken,
        final String cookieToken
    ) {
        return generatedToken.filter(gen -> gen.equalsIgnoreCase(cookieToken))
            .map(i -> true)
            .defaultIfEmpty(false);
    }
    
    public Mono<Long> getRank(
        final String queue,
        final Long userId
    ) {
        return reactiveRedisTemplate.opsForZSet()
            .rank(USER_QUEUE_WAIT_KEY.formatted(queue), userId.toString())
            .defaultIfEmpty(-1L)
            .map(rank -> rank >= 0 ? rank + 1 : rank);
    }
    
    public void scheduleAllowUser(Long maxAllowUser) {
        reactiveRedisTemplate.scan(
            ScanOptions.scanOptions()
                .match(USER_QUEUE_WAIT_KEY_FOR_SCAN)
                .count(100).build()
        )
            .map(key -> key.split(":")[2])
            .flatMap(
                queue -> allowUser(queue, maxAllowUser)
                    .map(allowed -> Tuples.of(queue, allowed))
            )
            .doOnNext(tuple -> log.info("queue: {}, tried: {}, allowed: {}", tuple.getT1(), maxAllowUser, tuple.getT2()))
            .subscribe();
    }
    
    public Mono<String> generateToken(
        final String queue,
        final Long userId
    ) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        var input = "user-queue-%s-%d".formatted(queue, userId);
        
        StringBuilder hexString = new StringBuilder();
        
        byte[] encodedHash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
        for (byte aByte : encodedHash) {
            hexString.append(String.format("%02x", aByte));
        }
        
        return Mono.just(hexString.toString());
    }
}
