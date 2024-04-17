package com.example.funnel.config.redis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.ReactiveRedisTemplate;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class RedisConfig implements ApplicationListener<ApplicationReadyEvent> {
    
    private final ReactiveRedisTemplate<String, String> reactiveRedisTemplate;
    
    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        reactiveRedisTemplate.opsForValue().get("1")
            .doOnSuccess(i -> log.info("initialize redis connection"))
            .doOnError(e -> log.error("fail to initialize redis connection: {}", e.getMessage()))
            .subscribe();
    }
}
