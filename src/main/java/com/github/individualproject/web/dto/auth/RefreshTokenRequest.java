package com.github.individualproject.web.dto.auth;

import lombok.Data;

@Data
public class RefreshTokenRequest {
    private String refreshToken;

    // 기본 생성자
    public RefreshTokenRequest() {}

    // 생성자
    public RefreshTokenRequest(String refreshToken) {
        this.refreshToken = refreshToken;
    }

}
