package com.github.individualproject.config.auth;

import com.github.individualproject.config.auth.JwtTokenProvider;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;


import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;


    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        // 사용자 정보에서 이메일 추출
        String email = authentication.getName();
        // 사용자 역할 가져오기 (예: ROLE_USER)
        List<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        // JWT 토큰 생성
        String token = jwtTokenProvider.createToken(email, roles);

        // 응답에 JWT 토큰 추가
        response.setHeader("Authorization", "Bearer " + token);
        response.getWriter().write("{\"status\":\"success\", \"token\":\"" + token + "\"}");
        response.setStatus(HttpServletResponse.SC_OK);
    }
}