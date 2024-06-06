package com.example.funnel.domain.user.model;

import lombok.*;

@Data
@Setter(AccessLevel.NONE)
@Builder
@ToString
public class RankNumberResponse {
    
    private Long rank;
}
