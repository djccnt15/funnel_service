package com.example.funnel.domain.user.converter;

import com.example.funnel.annotation.Converter;
import com.example.funnel.domain.user.model.RegisterUserResponse;

@Converter
public class UserQueueConverter {
    
    public RegisterUserResponse toRegisterUserResponse(Long userRank) {
        return RegisterUserResponse.builder()
            .rank(userRank)
            .build();
    }
}
