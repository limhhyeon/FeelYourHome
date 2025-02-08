package com.github.individualproject.web.dto.auth;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class SignUp {
    private String name;
    private String email;
    private String password;
    private String passwordCheck;
    private LocalDate birthday;
    private String phoneNumber;
}
