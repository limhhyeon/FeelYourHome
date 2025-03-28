package com.github.individualproject.web.controller;

import com.github.individualproject.service.auth.EmailService;
import com.github.individualproject.web.dto.ResponseDto;
import com.github.individualproject.web.dto.auth.EmailAuthNumCheck;
import com.github.individualproject.web.dto.auth.EmailCheck;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/email")
@Slf4j
@RequiredArgsConstructor
public class EmailController {
    private final EmailService emailService;

    @PostMapping("")
    public ResponseDto sendEmail(@RequestBody EmailCheck email){
        return emailService.sendEmailResult(email.getEmail());
    }
    @PostMapping("/auth-num-check")
    public ResponseDto authNumCheck(@RequestBody EmailAuthNumCheck check){
        return emailService.authNumCheckResult(check);
    }
}
