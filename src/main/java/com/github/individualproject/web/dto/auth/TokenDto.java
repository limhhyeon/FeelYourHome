package com.github.individualproject.web.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TokenDto {
    private String accessToken;
    private String refreshToken;


}
