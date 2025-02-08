package com.github.individualproject.web.controller;

import com.github.individualproject.repository.userDetails.CustomUserDetails;
import com.github.individualproject.service.auth.AuthService;
import com.github.individualproject.web.dto.ResponseDto;
import com.github.individualproject.web.dto.auth.*;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/sign-up")
    public ResponseDto signUp(@RequestBody SignUp signUp){
        return authService.signUpResult(signUp);
    }

    @PostMapping("/email")
    public ResponseDto CheckEmail(@RequestBody DuplicateCheck duplicateCheck){
        return authService.duplicateCheckResult(duplicateCheck);
    }

//    @PostMapping("/login")
//    public ResponseDto login(@RequestBody Login login, HttpServletResponse response){
//        String token = authService.loginResult(login);
//        response.setHeader("Authorization", "Bearer " + token);
//        return new ResponseDto(HttpStatus.OK.value(), "로그인 성공");
//    }

    @PostMapping("/login")
    public ResponseDto login(@RequestBody Login login, HttpServletResponse response) {
        TokenDto tokenDto = authService.loginResult(login);
        response.setHeader("Authorization", "Bearer " + tokenDto.getAccessToken());
        // 리프레시 토큰도 응답 본문에 포함
        return new ResponseDto(HttpStatus.OK.value(), "로그인 성공", tokenDto);
    }


    @PostMapping("/refresh")
    public ResponseDto refreshToken(@RequestBody RefreshTokenRequest refreshTokenRequest,HttpServletResponse response) {
        TokenDto tokenDto = authService.refreshToken(refreshTokenRequest.getRefreshToken());
        response.setHeader("Authorization", "Bearer " + tokenDto.getAccessToken());
        return new ResponseDto(HttpStatus.OK.value(), "토큰 갱신 성공", tokenDto);
    }

    @GetMapping("/test")
    public String test(@AuthenticationPrincipal CustomUserDetails customUserDetails){
        return customUserDetails.getEmail();
    }

}
