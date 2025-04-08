package com.github.individualproject.web.controller;

import com.github.individualproject.repository.user.CurrentUser;
import com.github.individualproject.repository.user.User;
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
@RequestMapping("api/auth")
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
    @GetMapping("/login-valid")
    public ResponseDto loginValid(@CurrentUser User user){
        return authService.loginValidRequest(user);
    }


    @PostMapping("/login")
    public ResponseDto login(@RequestBody Login loginRequest, HttpServletResponse response){
        log.info("동작 성공");
        String  token = authService.loginResult(loginRequest);
        if (token != null && !token.isEmpty()) {
            authService.createCookie(token,response);
            log.info("동작 성공");
            return new ResponseDto(HttpStatus.OK.value(), "로그인에 성공하였습니다.");
        } else {
            return new ResponseDto(HttpStatus.UNAUTHORIZED.value(), "아이디 또는 비밀번호를 다시 확인해주세요");
        }
    }
    @PostMapping("/logout")
    public ResponseDto logout(@CurrentUser User user, HttpServletResponse response){
        authService.logoutResult(user,response);

        return new ResponseDto(HttpStatus.OK.value(), "로그아웃 성공");
    }


    @PostMapping("/token/refresh")
    public ResponseDto refreshToken(@CookieValue(value = "Authorization") String accessToken,HttpServletResponse response){
        return authService.refreshToken(accessToken,response);
    }

    @GetMapping("/test")
    public String test(@CurrentUser User user){
        if (user == null) {
            System.out.println("User is null");
            return "User not authenticated";
        }
        System.out.println("user : " + user.getEmail());
        return user.getEmail();
    }


}
