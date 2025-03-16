package com.github.individualproject.web.filter;

import com.github.individualproject.config.auth.CustomErrorSend;
import com.github.individualproject.config.auth.JwtTokenProvider;
import com.github.individualproject.service.exception.TokenValidateException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.servlet.util.matcher.MvcRequestMatcher;
import org.springframework.security.web.util.matcher.AndRequestMatcher;
import org.springframework.security.web.util.matcher.NegatedRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;

import java.io.IOException;
@RequiredArgsConstructor
@Component
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtTokenProvider jwtTokenProvider;
    private final HandlerMappingIntrospector introspector;
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        RequestMatcher permitAllMatcher = new AndRequestMatcher(
                new OrRequestMatcher( // 먼저 허용할 URL들 정의
                        new MvcRequestMatcher(introspector, "/auth/**")
                ),
                new NegatedRequestMatcher( // 제외할 URL들
                        new OrRequestMatcher(
                                new MvcRequestMatcher(introspector, "/auth/logout"),
                                new MvcRequestMatcher(introspector, "/auth/test")
                        )
                )
        );
        if (permitAllMatcher.matches(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = jwtTokenProvider.resolveToken(request);

        try {
            if (token != null && jwtTokenProvider.validToken(token)) {
                Authentication auth = jwtTokenProvider.getAuthentication(token);
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        } catch (TokenValidateException e) {
            e.printStackTrace();
            CustomErrorSend.handleException(response, e.getMessage());

        }
        filterChain.doFilter(request,response);
    }
}

