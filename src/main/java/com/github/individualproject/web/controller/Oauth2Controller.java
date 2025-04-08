package com.github.individualproject.web.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.individualproject.config.auth.JwtTokenProvider;
import com.github.individualproject.repository.role.Role;
import com.github.individualproject.repository.user.User;
import com.github.individualproject.repository.user.UserRepository;
import com.github.individualproject.repository.userDetails.CustomUserDetails;
import com.github.individualproject.repository.userDetails.OAuth2UserInfo;
import com.github.individualproject.repository.userRole.UserRole;
//import com.github.individualproject.service.auth.Oauth2Service;
import com.github.individualproject.service.auth.AuthService;

import com.github.individualproject.service.exception.NotFoundException;
import com.github.individualproject.web.dto.ResponseDto;
import com.github.individualproject.web.dto.auth.KakaoUserInfo;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.relational.core.sql.In;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
//@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class Oauth2Controller {
//    private final Oauth2Service oauth2Service;
    private final AuthService authService;
    private final JwtTokenProvider jwtTokenProvider;


    //카카오 리다이렉트
//    @GetMapping("/oauth2/login/kakao")
//    public void kakaoLoginRedirect(HttpServletResponse response)  {
//        oauth2Service.redirect(response);
//
//    }
//    @GetMapping("/oauth2/callback/kakao")
//    public ResponseDto kakaoCallback(@RequestParam("code") String code, HttpServletResponse response) {
//        log.info("코드: " +code);
//        String accessToken = oauth2Service.getJwtToken(code);
//        authService.createCookie(accessToken,response);
//        // 여기서 accessToken을 사용하여 추가 작업을 수행할 수 있습니다.
//        log.info("토큰입니다: " + accessToken);
//        return new ResponseDto(HttpStatus.OK.value(), "로그인에 성공하였습니다.");
//
//    }

//    @GetMapping("/oauth2/callback/kakao")
//    public void kakaoCallback(@RequestParam("code") String code, HttpServletResponse response) throws IOException {
//        log.info("코드: " + code);
//        String accessToken = oauth2Service.getJwtToken(code);
//        authService.createCookie(accessToken, response);
//        log.info("토큰입니다: " + accessToken);
//        response.sendRedirect("http://localhost:5173/callback"); // 프론트엔드 콜백 경로
//    }
//    @GetMapping("/oauth2/callback/kakao")
//    public void kakaoCallback(@AuthenticationPrincipal OAuth2User oAuth2User,
//                              HttpServletResponse response) throws IOException {
//        if (oAuth2User == null) {
//            throw new IllegalStateException("인증 실패");
//        }
//
//        String email = oAuth2User.getAttribute("email");
//        List<String> roles = oAuth2User.getAuthorities().stream()
//                .map(authority -> authority.getAuthority())
//                .collect(Collectors.toList());
//
//        // JWT 토큰 생성
//        String accessToken = jwtTokenProvider.createToken(email, roles);
//        log.info("토큰입니다: " + accessToken);
//
//        // 쿠키 설정
//        authService.createCookie(accessToken, response);
//        log.info("여기까지는 오는 거니?");
//
//        // 프론트엔드로 리다이렉트 (쿼리 파라미터 제거)
//        response.sendRedirect("http://localhost:5173/");
//    }
//    @GetMapping("/oauth2/callback/kakao")
//    public ResponseDto kakaoCallback(@RequestParam("code") String code, HttpServletResponse response) {
//        log.info("코드: " +code);
//        String accessToken = oauth2Service.getJwtToken(code);
//
//        // 여기서 accessToken을 사용하여 추가 작업을 수행할 수 있습니다.
//        log.info("토큰입니다: " + accessToken);
//        response.setHeader("Authorization", "Bearer " + accessToken);
//        return new ResponseDto(HttpStatus.OK.value(), "로그인 성공 : " + "Bearer " + accessToken);
//    }
//@GetMapping("/oauth2/callback/kakao")
//public ResponseDto kakaoCallback(@AuthenticationPrincipal CustomUserDetails customUserDetails) {
//    ;
//    String email = customUserDetails.getEmail();
//    log.info("email : "+ email);
//
//    User user = userRepository.findByEmailFetchJoin(email)
//            .orElseThrow(()-> new NotFoundException("유저를 찾을 수 없습니다."));
//
//    List<String> role = user.getUserRoles().stream().map(UserRole::getRole).map(Role::getRoleName).collect(Collectors.toList());
//    // JWT 토큰 생성 (이 부분은 별도의 JwtTokenProvider 서비스를 사용하여 구현해야 합니다)
//    String jwtToken = jwtTokenProvider.createToken( email,role);
//
//    return new ResponseDto(HttpStatus.OK.value(), "로그인 성공 : Bearer " + jwtToken);
//}
}
