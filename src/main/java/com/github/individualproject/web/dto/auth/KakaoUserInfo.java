package com.github.individualproject.web.dto.auth;

import lombok.*;

import java.util.Map;

@Getter
@AllArgsConstructor
@ToString
@Builder
public class KakaoUserInfo {
    private Long id;
    private String email;
    private String nickname;

}
