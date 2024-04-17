package com.example.funnel.domain.user.model;

import lombok.*;

@Data
@Setter(AccessLevel.NONE)
@Builder
@ToString
public class RegisterUserResponse {
    
    private Long rank;
}
