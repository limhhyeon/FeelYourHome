package com.github.individualproject.web.dto.auth;

import lombok.*;

@Getter
@AllArgsConstructor
@ToString
@Builder
public class EmailCheck {
    private final String email;
}
