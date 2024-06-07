# funnel_service

funner service with Redis, Spring Webflux

## OpenAPI swagger-ui 

- [funner server](http://127.0.0.1:8080/webjars/swagger-ui/index.html)
- [main server](http://127.0.0.1:8000/swagger-ui/index.html)

## Architecture

- Redis: 접속 대기열 관리
- Embedded Redis: 테스트 전용 내장 레디스

```mermaid
sequenceDiagram
    User ->> Spring MVC: request login
    activate Spring MVC
    critical 웹페이지 진입 가능 여부 확인
    Spring MVC ->> Spring Webflux: check available
    activate Spring Webflux
    Spring Webflux -) Redis: check sequence
    activate Redis
    Redis --) Spring Webflux: response sequence
    deactivate Redis
    Spring Webflux --) Spring MVC: response available
    deactivate Spring Webflux
    end
    Spring MVC ->> Spring Webflux: check available
    deactivate Spring MVC
    activate Spring Webflux
    Spring Webflux -) Redis: check sequence
    activate Redis
    Redis --) Spring Webflux: response sequence
    deactivate Redis
    Spring Webflux --) User: response
    deactivate Spring Webflux
    critical 웹페이지 진입 가능 여부 확인 및 Redirect
    User ->> Spring Webflux: request
    activate Spring Webflux
    Spring Webflux -) Redis: request
    activate Redis
    Redis --) Spring Webflux: response
    deactivate Redis
    Spring Webflux --) User: response
    deactivate Spring Webflux
    end
```

## Memo

- cookie는 포트까지는 확인하지 않고 도메인 기반으로 작동하기 때문에, 서로 다른 서버에서 생성된 cookie를 활용할 수 있음