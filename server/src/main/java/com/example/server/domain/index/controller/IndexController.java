package com.example.server.domain.index.controller;

import com.example.server.domain.index.business.IndexBusiness;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping(path = "/")
@RequiredArgsConstructor
public class IndexController {
    
    private final IndexBusiness indexBusiness;
    
    @GetMapping
    public String index(
        @RequestParam(name = "queue", defaultValue = "default") String queue,
        @RequestParam(name = "user_id") Long userId,
        HttpServletRequest servletRequest
    ) {
        return indexBusiness.checkAvailable(queue, userId, servletRequest);
    }
}
