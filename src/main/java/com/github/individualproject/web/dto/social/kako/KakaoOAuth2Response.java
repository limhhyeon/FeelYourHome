package com.github.individualproject.web.dto.social.kako;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@AllArgsConstructor
@ToString
@Builder
public class KakaoOAuth2Response {
    private Long id;
    @JsonProperty("kakao_account")
    private KakaoAccount kakaoAccount;
    private Properties properties;
}
