package com.example.server.domain.index.business;

import com.example.server.annotation.Business;
import com.example.server.domain.index.model.AllowedUserResponse;
import com.example.server.domain.index.service.IndexService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

@Business
@RequiredArgsConstructor
public class IndexBusiness {
    
    private final IndexService indexService;
    
    public String checkAvailable(String queue, Long userId, HttpServletRequest servletRequest) {
        var token = indexService.getTokenFromCookie(queue, servletRequest);
        var uri = indexService.createAvailableCheckUri(queue, userId, token);
        
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<AllowedUserResponse> response = restTemplate.getForEntity(uri, AllowedUserResponse.class);
        
        if (response.getBody() == null || !response.getBody().getAllowed()) {
            return "redirect:http://localhost:8080/waiting-room?user_id=%d&redirect_url=%s".formatted(
                userId, "http://localhost:8000?user_id=%d".formatted(userId)
            );
        }
        return "index";
    }
}
