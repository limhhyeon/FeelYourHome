package com.github.individualproject.service.auth;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.individualproject.config.auth.JwtTokenProvider;
import com.github.individualproject.repository.refreshToken.RefreshTokenRepository;
import com.github.individualproject.repository.role.Role;
import com.github.individualproject.repository.role.RoleRepository;
import com.github.individualproject.repository.user.User;
import com.github.individualproject.repository.user.UserRepository;
import com.github.individualproject.repository.userRole.UserRole;
import com.github.individualproject.repository.userRole.UserRoleRepository;
import com.github.individualproject.service.exception.BadRequestException;
import com.github.individualproject.service.exception.NotFoundException;
import com.github.individualproject.web.dto.auth.SocialLoginUser;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class Oauth2Service {
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    @Value("${spring.security.oauth2.client.registration.kakao.client-id}")
    private String clientId;
    @Value("${spring.security.oauth2.client.registration.kakao.client-secret}")
    private String clientSecret;
    @Value("${spring.security.oauth2.client.registration.kakao.redirect-uri}")
    private String redirectUri;
    private final RefreshTokenRepository refreshTokenRepository;
    private final AuthService authService;


    @Transactional
    public String getJwtToken(String code) {
        String kakaoAccessToken = getAccessToken(code);
        SocialLoginUser kakaoUserInfo = getKakaoUserInfo(kakaoAccessToken);
        return isSocialLoginResult(kakaoUserInfo);
    }

    private String isSocialLoginResult(SocialLoginUser kakaoUserInfo) {
        if (userRepository.existsByEmail(kakaoUserInfo.getEmail())){
            User user = userRepository.findByEmailWithRoles(kakaoUserInfo.getEmail())
                    .orElseThrow(()-> new NotFoundException("유저를 찾을 수 없습니다."));
            List<String> roles = user.getUserRoles().stream()
                    .map(UserRole::getRole)
                    .map(Role::getRoleName)
                    .collect(Collectors.toList());
            if (!refreshTokenRepository.existsByUser(user)){
                authService.createRefreshToken(user,kakaoUserInfo.getEmail());
            }
            return jwtTokenProvider.createToken(kakaoUserInfo.getEmail(), roles);
        }else {
            Role role = roleRepository.findByRoleName("ROLE_USER")
                    .orElseThrow(() -> new NotFoundException("역할이 존재하지 않습니다."));
            User user = User.builder()
                    .signupType("카카오")
                    .birthday(LocalDate.now())
                    .email(kakaoUserInfo.getEmail())
                    .name(kakaoUserInfo.getNickname())
                    .phoneNumber("카카오 유저")
                    .build();
            User saveUser =userRepository.save(user);
            UserRole userRole = UserRole.createUserRole(role,user);
            userRoleRepository.save(userRole);
            User findUser = userRepository.findByEmailWithRoles(saveUser.getEmail())
                    .orElseThrow(()-> new NotFoundException("유저를 찾을 수 없습니다."));
            List<String> roles = userRoleRepository.findUserRolesByUser(findUser)
                    .stream()
                    .map(UserRole::getRole).map(Role::getRoleName).collect(Collectors.toList());
            System.out.println("저장된 유저입니다. : "+roles);
            if (!refreshTokenRepository.existsByUser(user)){
                authService.createRefreshToken(user,kakaoUserInfo.getEmail());
            }

            return jwtTokenProvider.createToken(user.getEmail(),roles);

        }
    }

    private SocialLoginUser getKakaoUserInfo(String kakaoAccessToken) {


        // HTTP Header 생성
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + kakaoAccessToken);
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        // HTTP 요청 보내기
        HttpEntity<MultiValueMap<String, String>> kakaoUserInfoRequest = new HttpEntity<>(headers);
        RestTemplate rt = new RestTemplate();
        ResponseEntity<String> response = rt.exchange(
                "https://kapi.kakao.com/v2/user/me",
                HttpMethod.POST,
                kakaoUserInfoRequest,
                String.class
        );

        // responseBody에 있는 정보를 꺼냄
        String responseBody = response.getBody();
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = null;
        try {
            jsonNode = objectMapper.readTree(responseBody);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        Long id = jsonNode.get("id").asLong();
        String email = jsonNode.get("kakao_account").get("email").asText();
        String nickname = jsonNode.get("properties").get("nickname").asText();

        SocialLoginUser socialLoginUser = new SocialLoginUser(id,email,nickname);

        return socialLoginUser;
    }

    public String getAccessToken(String code) {


        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("client_id", clientId);
        body.add("redirect_uri", redirectUri);
        body.add("code", code);
        body.add("client_secret", clientSecret);

        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(body, headers);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.exchange("https://kauth.kakao.com/oauth/token", HttpMethod.POST, requestEntity, String.class);

//        return (String) response.getBody().get("access_token"); // 액세스 토큰 반환
        // HTTP 응답 (JSON) -> 액세스 토큰 파싱
        String responseBody = response.getBody();
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = null;
        try {
            jsonNode = objectMapper.readTree(responseBody);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return jsonNode.get("access_token").asText(); //토큰 전송
    }


    public void redirect(HttpServletResponse response) {


        String authUrl = "https://kauth.kakao.com/oauth/authorize?response_type=code&client_id=" + clientId + "&redirect_uri=" + redirectUri + "&through_account=true&additional_auth_login=true";
        try{
            response.sendRedirect(authUrl); // 카카오 로그인 페이지로 리디렉션
        }catch (IOException ioe){
            throw new NotFoundException("잘못된 주소입니다.");
        }

    }
}
