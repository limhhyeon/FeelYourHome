package com.github.individualproject.config.auth;

import com.github.individualproject.config.auth.JwtTokenProvider;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;


import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private final JwtTokenProvider jwtTokenProvider;
    @Value("${front.redirect}")
    private String redirectAddress;


    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        // 내가 담은 Oauth2User에서 이메일 추출
        String email = oAuth2User.getAttribute("email");

        //권한 추출
        List<String> roles = oAuth2User.getAuthorities().stream()
                .map(authority -> authority.getAuthority())
                .collect(Collectors.toList());
        // JWT 토큰 생성
        String accessToken = jwtTokenProvider.createToken(email, roles);
        createCookie(accessToken,response);

        // 프론트엔드 메인페이지로 리다이렉트
        response.sendRedirect(redirectAddress);

    }
    public void createCookie(String newAccessToken,HttpServletResponse response){
        ResponseCookie cookie = ResponseCookie.from("Authorization", newAccessToken)
                .httpOnly(true)
                .secure(true) // https 환경에서 true로 설정
                .path("/")
                .maxAge(60 * 60 * 24)
                .sameSite("Lax") // SameSite 설정
                .build();
        response.addHeader("Set-Cookie",cookie.toString());
    }
}