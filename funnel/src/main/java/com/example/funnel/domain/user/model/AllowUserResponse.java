package com.example.funnel.domain.user.model;

import lombok.*;

@Data
@Setter(AccessLevel.NONE)
@Builder
@ToString
public class AllowUserResponse {
    
    private Long requestCount;
    
    private Long allowedCount;
}
