package com.github.individualproject.web.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

@Data
@AllArgsConstructor
public class SocialLoginUser {
    private Long id;
    private String email;
    private String nickname;



}
