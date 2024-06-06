package com.example.funnel.domain.user.model;

import lombok.*;

@Data
@Setter(AccessLevel.NONE)
@Builder
@ToString
public class AllowedUserResponse {

    private Boolean allowed;
}
