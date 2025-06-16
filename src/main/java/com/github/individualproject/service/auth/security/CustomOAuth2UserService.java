package com.github.individualproject.service.auth.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.individualproject.config.auth.JwtTokenProvider;
import com.github.individualproject.repository.refreshToken.RefreshToken;
import com.github.individualproject.repository.refreshToken.RefreshTokenRepository;
import com.github.individualproject.repository.role.Role;
import com.github.individualproject.repository.role.RoleRepository;
import com.github.individualproject.repository.user.User;
import com.github.individualproject.repository.user.UserRepository;
import com.github.individualproject.repository.userRole.UserRole;
import com.github.individualproject.repository.userRole.UserRoleRepository;
import com.github.individualproject.service.exception.NotFoundException;
import com.github.individualproject.web.dto.social.kako.KakaoOAuth2Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
@Service
@RequiredArgsConstructor
@Slf4j
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final RoleRepository roleRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final ObjectMapper objectMapper;
//    private final AuthService authService;

    private final JwtTokenProvider jwtTokenProvider;
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        // kakao_account와 properties에서 데이터 추출
        KakaoOAuth2Response kakaoResponse = objectMapper.convertValue(oAuth2User.getAttributes(), KakaoOAuth2Response.class);

        log.info("전체 확인 : "+kakaoResponse);
        String email = kakaoResponse.getKakaoAccount() != null ? kakaoResponse.getKakaoAccount().getEmail() : null;
        String nickname = extractNickname(kakaoResponse);
        log.info("이메일 확인" +email);

        // kakao_account.profile에서도 nickname 확인 (중복 확인)
        if (email == null || nickname == null) {
            throw new OAuth2AuthenticationException("Kakao 응답에서 필수 정보(email 또는 nickname)를 가져올 수 없습니다.");
        }

        User user;
        if (userRepository.existsByEmail(email)) {
            user = userRepository.findByEmailWithRoles(email)
                    .orElseThrow(() -> new NotFoundException("유저를 찾을 수 없습니다."));
        } else {
            Role role = roleRepository.findByRoleName("ROLE_USER")
                    .orElseThrow(() -> new NotFoundException("역할이 존재하지 않습니다."));
            user = User.builder()
                    .signupType("카카오")
                    .birthday(LocalDate.now())
                    .email(email)
                    .name(nickname)
                    .phoneNumber("카카오 유저")
                    .build();
            User savedUser = userRepository.save(user);
            UserRole userRole = UserRole.createUserRole(role, savedUser);
            userRoleRepository.save(userRole);
            user = userRepository.findByEmailWithRoles(savedUser.getEmail())
                    .orElseThrow(() -> new NotFoundException("유저를 찾을 수 없습니다."));
        }

        if (!refreshTokenRepository.existsByUser(user)) {
            createRefreshToken(user, email);
        }

        List<SimpleGrantedAuthority> authorities = user.getUserRoles().stream()
                .map(userRole -> new SimpleGrantedAuthority(userRole.getRole().getRoleName()))
                .collect(Collectors.toList());
        Map<String, Object> attributes = new HashMap<>(oAuth2User.getAttributes());
        attributes.put("email", email);
        attributes.put("nickname", nickname);

        return new DefaultOAuth2User(
                authorities,
                attributes,
                "id" // application.yml의 user-name-attribute와 일치
        );
    }

    public void createRefreshToken(User user,String email){
        String refresh = jwtTokenProvider.createRefreshToken(email);
        RefreshToken refreshToken = RefreshToken.of(user,refresh);
        System.out.println(refreshToken);
        refreshTokenRepository.save(refreshToken);
    }
    private String extractNickname(KakaoOAuth2Response response) {
        // properties에서 nickname 가져오기
        if (response.getProperties() != null && response.getProperties().getNickname() != null) {
            return response.getProperties().getNickname();
        }
        // kakao_account.profile에서 nickname 가져오기
        if (response.getKakaoAccount() != null && response.getKakaoAccount().getProfile() != null) {
            return response.getKakaoAccount().getProfile().getNickname();
        }
        return null;
    }
}