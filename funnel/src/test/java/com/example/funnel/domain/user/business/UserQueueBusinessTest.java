package com.example.funnel.domain.user.business;

import com.example.funnel.EmbeddedRedis;
import com.example.funnel.domain.user.model.AllowUserResponse;
import com.example.funnel.domain.user.model.AllowedUserResponse;
import com.example.funnel.domain.user.model.RegisterUserResponse;
import com.example.funnel.exception.ApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.connection.ReactiveRedisConnection;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import reactor.test.StepVerifier;

@SpringBootTest
@Import(EmbeddedRedis.class)
@ActiveProfiles("test")
class UserQueueBusinessTest {
    
    private final String testQueue = "default";
    
    @Autowired
    private UserQueueBusiness userQueueBusiness;
    
    @Autowired
    private ReactiveRedisTemplate<String, String> reactiveRedisTemplate;
    
    @BeforeEach
    public void beforeEach() {
        // 각 테스트 진행 후 Redis에 먼저 진행한 테스트의 데이터가 남기 때문에 의도치 않은 에러 발생
        // @BeforeEach를 통해 각각의 테스트 진행 전에 실행해줘야 하는 메서드 추가
        ReactiveRedisConnection redisConnection = reactiveRedisTemplate.getConnectionFactory()
            .getReactiveConnection();
        redisConnection.serverCommands().flushAll().subscribe();
    }
    
    @Test
    void registerUser() {
        StepVerifier
            .create(userQueueBusiness.registerUser(testQueue, 100L))
            .expectNext(RegisterUserResponse.builder().rank(1L).build())
            .verifyComplete();
        
        StepVerifier
            .create(userQueueBusiness.registerUser(testQueue, 101L))
            .expectNext(RegisterUserResponse.builder().rank(2L).build())
            .verifyComplete();
        
        StepVerifier
            .create(userQueueBusiness.registerUser(testQueue, 102L))
            .expectNext(RegisterUserResponse.builder().rank(3L).build())
            .verifyComplete();
    }
    
    @Test
    void alreadyRegisteredUser() {
        StepVerifier
            .create(userQueueBusiness.registerUser(testQueue, 100L))
            .expectNext(RegisterUserResponse.builder().rank(1L).build())
            .verifyComplete();
        
        StepVerifier
            .create(userQueueBusiness.registerUser(testQueue, 100L))
            .expectError(ApiException.class)
            .verify();
    }
    
    @Test
    void emptyAllowUser() {
        StepVerifier
            .create(userQueueBusiness.allowUser(testQueue, 3L))
            .expectNext(
                AllowUserResponse.builder()
                    .requestCount(3L)
                    .allowedCount(0L)
                    .build()
            )
            .verifyComplete();
    }
    
    @Test
    void allowUser() {
        StepVerifier
            .create(
                userQueueBusiness.registerUser(testQueue, 100L)
                .then(userQueueBusiness.registerUser(testQueue, 101L))
                .then(userQueueBusiness.registerUser(testQueue, 102L))
                .then(userQueueBusiness.allowUser(testQueue, 2L))
            )
            .expectNext(
                AllowUserResponse.builder()
                    .requestCount(2L)
                    .allowedCount(2L)
                    .build()
            )
            .verifyComplete();
    }
    
    @Test
    void allowUser2() {
        StepVerifier
            .create(
                userQueueBusiness.registerUser(testQueue, 100L)
                .then(userQueueBusiness.registerUser(testQueue, 101L))
                .then(userQueueBusiness.registerUser(testQueue, 102L))
                .then(userQueueBusiness.allowUser(testQueue, 5L))
            )
            .expectNext(
                AllowUserResponse.builder()
                    .requestCount(5L)
                    .allowedCount(3L)
                    .build()
            )
            .verifyComplete();
    }
    
    @Test
    void allowUserAfterRegisterWaitQueue() {
        StepVerifier
            .create(
                userQueueBusiness.registerUser(testQueue, 100L)
                    .then(userQueueBusiness.registerUser(testQueue, 101L))
                    .then(userQueueBusiness.registerUser(testQueue, 102L))
                    .then(userQueueBusiness.allowUser(testQueue, 3L))
                    .then(userQueueBusiness.registerUser(testQueue, 200L))
            )
            .expectNext(RegisterUserResponse.builder().rank(1L).build())
            .verifyComplete();
    }
    
    @Test
    void isNotAllowedUser() {
        StepVerifier
            .create(userQueueBusiness.isAllowedUser(testQueue, 100L))
            .expectNext(AllowedUserResponse.builder().allowed(false).build())
            .verifyComplete();
    }
    
    @Test
    void isNotAllowedUser2() {
        StepVerifier
            .create(
                userQueueBusiness.registerUser(testQueue, 100L)
                    .then(userQueueBusiness.allowUser(testQueue, 1L))
                    .then(userQueueBusiness.isAllowedUser(testQueue, 101L))
            )
            .expectNext(AllowedUserResponse.builder().allowed(false).build())
            .verifyComplete();
    }
    
    @Test
    void isAllowedUser() {
        StepVerifier
            .create(
                userQueueBusiness.registerUser(testQueue, 100L)
                    .then(userQueueBusiness.allowUser(testQueue, 1L))
                    .then(userQueueBusiness.isAllowedUser(testQueue, 100L))
            )
            .expectNext(AllowedUserResponse.builder().allowed(true).build())
            .verifyComplete();
    }
}