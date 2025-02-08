package com.github.individualproject.web.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

@Data
@AllArgsConstructor
public class KakaoUserInfo {
    private Long id;
    private String email;
    private String nickname;

}
