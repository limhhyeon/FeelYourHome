package com.github.individualproject.web.dto.auth;

import lombok.*;

@Getter
@AllArgsConstructor
@ToString
@Builder
public class TokenDto {
    private String accessToken;
    private String refreshToken;


}
