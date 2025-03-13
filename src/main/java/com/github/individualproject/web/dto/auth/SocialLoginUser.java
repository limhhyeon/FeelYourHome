package com.github.individualproject.web.dto.auth;

import lombok.*;

import java.util.Map;

@Getter
@AllArgsConstructor
@ToString
@Builder
public class SocialLoginUser {
    private final Long id;
    private final String email;
    private final String nickname;



}
