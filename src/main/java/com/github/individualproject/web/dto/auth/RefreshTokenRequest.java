package com.github.individualproject.web.dto.auth;

import lombok.*;

@Getter
@Builder
@ToString
@AllArgsConstructor
public class RefreshTokenRequest {
    private String refreshToken;


}
