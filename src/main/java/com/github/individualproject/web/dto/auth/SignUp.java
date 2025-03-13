package com.github.individualproject.web.dto.auth;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
@ToString
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class SignUp {
    private final String name;
    private final String email;
    private final String password;
    private final String passwordCheck;
    private final LocalDate birthday;
    private final String phoneNumber;
}
