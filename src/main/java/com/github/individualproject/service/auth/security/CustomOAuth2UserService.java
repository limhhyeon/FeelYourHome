//package com.github.individualproject.service.auth.security;
//
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.databind.JsonNode;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.github.individualproject.config.JwtTokenProvider;
//import com.github.individualproject.repository.role.Role;
//import com.github.individualproject.repository.role.RoleRepository;
//import com.github.individualproject.repository.user.User;
//import com.github.individualproject.repository.user.UserRepository;
//import com.github.individualproject.repository.userDetails.CustomUserDetails;
//import com.github.individualproject.repository.userDetails.KakaoUserDetails;
//import com.github.individualproject.repository.userDetails.OAuth2UserInfo;
//import com.github.individualproject.repository.userRole.UserRole;
//import com.github.individualproject.repository.userRole.UserRoleRepository;
//import com.github.individualproject.service.exception.NotFoundException;
//import com.github.individualproject.web.dto.auth.SocialLoginUser;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.http.HttpEntity;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.HttpMethod;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.authority.SimpleGrantedAuthority;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
//import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
//import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
//import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
//import org.springframework.security.oauth2.core.OAuth2Error;
//import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
//import org.springframework.security.oauth2.core.user.OAuth2User;
//import org.springframework.stereotype.Service;
//import org.springframework.util.MultiValueMap;
//import org.springframework.web.client.RestTemplate;
//
//import java.time.LocalDate;
//import java.util.Collections;
//import java.util.List;
//import java.util.Map;
//import java.util.stream.Collectors;
//
//@Service
//@RequiredArgsConstructor
//@Slf4j
//public class CustomOAuth2UserService implements OAuth2UserService {
//
//    private final UserRepository userRepository;
//    private final JwtTokenProvider jwtTokenProvider;
//    private final RoleRepository roleRepository;
//    private final UserRoleRepository userRoleRepository;
//
//
//
//    @Override
//    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
//        String accessToken = userRequest.getAccessToken().getTokenValue();
//        log.info("카카오 요청 : " + accessToken);
//        SocialLoginUser kakaoUserInfo = getKakaoUserInfo(accessToken);
//
//        // 사용자 정보 처리 및 JWT 토큰 생성
//        return handleSocialLogin(kakaoUserInfo);
//    }
//
//    private SocialLoginUser getKakaoUserInfo(String accessToken) {
//        // Kakao API 호출하여 사용자 정보 가져오는 로직 (기존 getKakaoUserInfo 메서드 활용)
//        // HTTP Header 생성
//        HttpHeaders headers = new HttpHeaders();
//        headers.add("Authorization", "Bearer " + accessToken);
//        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");
//
//        // HTTP 요청 보내기
//        HttpEntity<MultiValueMap<String, String>> kakaoUserInfoRequest = new HttpEntity<>(headers);
//        RestTemplate rt = new RestTemplate();
//        ResponseEntity<String> response = rt.exchange(
//                "https://kapi.kakao.com/v2/user/me",
//                HttpMethod.POST,
//                kakaoUserInfoRequest,
//                String.class
//        );
//
//        // responseBody에 있는 정보를 꺼냄
//        String responseBody = response.getBody();
//        ObjectMapper objectMapper = new ObjectMapper();
//        JsonNode jsonNode;
//
//        try {
//            jsonNode = objectMapper.readTree(responseBody);
//            Long id = jsonNode.get("id").asLong();
//            String email = jsonNode.get("kakao_account").get("email").asText();
//            String nickname = jsonNode.get("properties").get("nickname").asText();
//            Map<String, Object> attributes = Map.of(
//                    "id", id,
//                    "email", email,
//                    "nickname", nickname
//            );
//
//            return new SocialLoginUser(id, email, nickname,attributes);
//
//        } catch (JsonProcessingException e) {
//            throw new RuntimeException("Failed to parse Kakao user info", e);
//        }
//    }
//
//    private OAuth2User handleSocialLogin(SocialLoginUser kakaoUserInfo) {
//        if (userRepository.existsByEmail(kakaoUserInfo.getEmail())) {
//            User user = userRepository.findByEmailFetchJoin(kakaoUserInfo.getEmail())
//                    .orElseThrow(() -> new NotFoundException("유저를 찾을 수 없습니다."));
//            List<String> roles = user.getUserRoles().stream()
//                    .map(UserRole::getRole)
//                    .map(Role::getRoleName)
//                    .collect(Collectors.toList());
//
//            String token = jwtTokenProvider.createToken(kakaoUserInfo.getEmail(), roles);
//            return new DefaultOAuth2User(Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")), kakaoUserInfo.getAttributes(), "id");
//
//        } else {
//            Role role = roleRepository.findByRoleName("ROLE_USER")
//                    .orElseThrow(() -> new NotFoundException("역할이 존재하지 않습니다."));
//            User user = User.builder()
//                    .signupType("카카오")
//                    .birthday(LocalDate.now())
//                    .email(kakaoUserInfo.getEmail())
//                    .name(kakaoUserInfo.getNickname())
//                    .phoneNumber("카카오 유저")
//                    .build();
//            User saveUser = userRepository.save(user);
//            UserRole userRole = UserRole.createUserRole(role, saveUser);
//            userRoleRepository.save(userRole);
//
//            List<String> roles = Collections.singletonList(role.getRoleName());
//            String token = jwtTokenProvider.createToken(user.getEmail(), roles);
//
//            return new DefaultOAuth2User(Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")), kakaoUserInfo.getAttributes(), "id");
//        }
//    }
//}
//
