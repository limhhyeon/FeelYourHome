package com.github.individualproject.config.auth;

//import com.github.individualproject.service.auth.security.CustomOAuth2UserService;
import com.github.individualproject.config.auth.JwtTokenProvider;
import com.github.individualproject.config.auth.CustomAuthenticationSuccessHandler;
import com.github.individualproject.config.auth.CustomerAccessDeniedHandler;
import com.github.individualproject.config.auth.CustomerAuthenticationEntryPoint;
import com.github.individualproject.repository.role.RoleRepository;
import com.github.individualproject.repository.user.UserRepository;
import com.github.individualproject.repository.userRole.UserRoleRepository;
//import com.github.individualproject.service.auth.security.CustomOAuth2UserService;
//import com.github.individualproject.service.auth.OAuth2LoginSuccessHandler;
//import com.github.individualproject.service.auth.security.CustomOAuth2UserService;
//import com.github.individualproject.service.auth.CustomOAuth2UserService;
//import com.github.individualproject.service.auth.OAuth2LoginSuccessHandler;
import com.github.individualproject.service.auth.security.CustomOAuth2UserService;
import com.github.individualproject.web.filter.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfiguration {
    private final JwtTokenProvider jwtTokenProvider;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;


    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http

                .headers((h)->h.frameOptions((f)->f.sameOrigin()))
                .csrf((c)->c.disable())
                .httpBasic(hb->hb.disable())
                .formLogin((fl)->fl.disable())
                .rememberMe((rm)->rm.disable())
                .sessionManagement(sm->sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authz -> authz
                        .anyRequest().permitAll() // 모든 요청 허용
                )
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService)
                        )
                        .successHandler(oAuth2LoginSuccessHandler)
                        .redirectionEndpoint(redirection -> redirection
                                .baseUri("/oauth2/callback/*")
                        )
                )
                .cors(c-> c.configurationSource(corsConfig()))
                .exceptionHandling((exception) -> exception
                        .authenticationEntryPoint(new CustomerAuthenticationEntryPoint())
                        .accessDeniedHandler(new CustomerAccessDeniedHandler()))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
    private CorsConfigurationSource corsConfig() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.setAllowCredentials(true);
//        corsConfiguration.setAllowedOrigins(List.of("*"));
        corsConfiguration.addAllowedOrigin("http://localhost:5173");
        corsConfiguration.addAllowedOrigin("https://localhost:3000");
        corsConfiguration.addAllowedOrigin("https://localhost:8080");
        corsConfiguration.addAllowedOrigin("http://localhost:8080");
        corsConfiguration.addAllowedHeader("*");
        corsConfiguration.addExposedHeader("Authorization");
        corsConfiguration.addExposedHeader("Set-Cookie");
//        corsConfiguration.setExposedHeaders(Arrays.asList("Authorization", "Authorization-refresh", "token","Set-Cookie"));
        corsConfiguration.setAllowedMethods(List.of("GET","PUT","POST","DELETE","OPTIONS"));
        corsConfiguration.setMaxAge(1000L*60*60);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**",corsConfiguration);
        return source;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
//    @Bean
//    public OAuth2UserService<OAuth2UserRequest, OAuth2User> kakaoOAuth2UserService() {
//        return new CustomOAuth2UserService(userRepository,jwtTokenProvider,roleRepository,userRoleRepository);
//    }


}
