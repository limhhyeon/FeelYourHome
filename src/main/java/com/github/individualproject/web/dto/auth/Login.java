package com.github.individualproject.web.dto.auth;

import lombok.*;

@Getter
@Builder
@ToString
@AllArgsConstructor
public class Login {
    private String email;
    private String password;
}
