package com.github.individualproject.web.dto.auth;

import com.github.individualproject.repository.user.User;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString
public class UserInfo {
    private final Integer userId;
    private final String nickname;
    private final String email;


    // 기존 of() 메서드 (imageUrl 포함)
    public static UserInfo of(User user) {
        return UserInfo.builder()
                .userId(user.getUserId())
                .nickname(user.getName())
                .email(user.getEmail())
                .build();
    }
}
