package com.example.funnel.domain.user.business;

import com.example.funnel.EmbeddedRedis;
import com.example.funnel.domain.user.model.AllowUserResponse;
import com.example.funnel.domain.user.model.AllowedUserResponse;
import com.example.funnel.domain.user.model.RankNumberResponse;
import com.example.funnel.domain.user.model.RegisterUserResponse;
import com.example.funnel.exception.ApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.connection.ReactiveRedisConnection;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.test.context.ActiveProfiles;
import reactor.test.StepVerifier;

import java.security.NoSuchAlgorithmException;

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
    
    @Test
    void getRank() {
        Long userId1 = 100L;
        Long userId2 = 101L;
        
        StepVerifier
            .create(userQueueBusiness.registerUser(testQueue, userId1)
                .then(userQueueBusiness.getRank(testQueue, userId1))
            )
            .expectNext(RankNumberResponse.builder().rank(1L).build())
            .verifyComplete();
        
        StepVerifier
            .create(userQueueBusiness.registerUser(testQueue, userId2)
                .then(userQueueBusiness.getRank(testQueue, userId2))
            )
            .expectNext(RankNumberResponse.builder().rank(2L).build())
            .verifyComplete();
    }
    
    @Test
    void emptyRank() {
        StepVerifier
            .create(userQueueBusiness.getRank(testQueue, 100L))
            .expectNext(RankNumberResponse.builder().rank(-1L).build())
            .verifyComplete();
    }
    
    @Test
    void isNotAllowedByToken() throws NoSuchAlgorithmException {
        StepVerifier
            .create(userQueueBusiness.isAllowedByToken(testQueue, 100L, ""))
            .expectNext(AllowedUserResponse.builder().allowed(false).build())
            .verifyComplete();
    }
    
    @Test
    void isAllowedByToken() throws NoSuchAlgorithmException {
        StepVerifier
            .create(userQueueBusiness.isAllowedByToken(testQueue, 100L, "d333a5d4eb24f3f5cdd767d79b8c01aad3cd73d3537c70dec430455d37afe4b8"))
            .expectNext(AllowedUserResponse.builder().allowed(true).build())
            .verifyComplete();
    }
    
    @Test
    void generateToken() throws NoSuchAlgorithmException {
        MockServerWebExchange exchange = MockServerWebExchange.from(
            MockServerHttpRequest.get("/")
        );
        
        // SHA-256 알고리즘은 입력값이 같을 경우 항상 동일한 결과값을 출력함
        StepVerifier
            .create(userQueueBusiness.generateToken(testQueue, 100L, exchange))
            .expectNext("d333a5d4eb24f3f5cdd767d79b8c01aad3cd73d3537c70dec430455d37afe4b8")
            .verifyComplete();
    }
}