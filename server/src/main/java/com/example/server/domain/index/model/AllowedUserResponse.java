package com.example.server.domain.index.model;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Setter(AccessLevel.NONE)
@Builder
@ToString
public class AllowedUserResponse {

    private Boolean allowed;
}
