package com.github.individualproject.service.auth;

import com.github.individualproject.web.dto.ResponseDto;
import com.github.individualproject.web.dto.auth.EmailAuthNumCheck;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender sender;
    @Value("${spring.mail.username}")
    private String senderEmail;
    private final RedisUtil redisUtil;

    public int generateVerificationNumber() {
        return (int)(Math.random() * (90000)) + 100000; // 100000에서 999999 사이의 숫자 생성
    }
    @Transactional
    public ResponseDto sendEmailResult(String email) {
        int sendNum = generateVerificationNumber();
        sender.send(createMessage(email,sendNum));
        redisUtil.setData(email,Integer.toString(sendNum),60*5L);
        return new ResponseDto(HttpStatus.OK.value(),"이메일 전송 성공");

    }
    public MimeMessage createMessage(String email, int sendNum){

        //메시지 생성
        MimeMessage mimeMessage = sender.createMimeMessage();
        try{
            mimeMessage.setFrom(senderEmail);
            mimeMessage.setRecipients(MimeMessage.RecipientType.TO,email);
            mimeMessage.setSubject("[이메일 인증 번호입니다.]");
            String body = sendNum + "";
            mimeMessage.setText(body,"UTF-8", "html");

        }
        catch (MessagingException e) {
            throw new RuntimeException(e);
        }catch (Exception e){
            e.printStackTrace();
        }
        return mimeMessage;
    }

    public ResponseDto authNumCheckResult(EmailAuthNumCheck check) {
        String authNum = Integer.toString(check.getAuthNum());
        if (redisUtil.getData(check.getEmail())== null){
            return new ResponseDto(HttpStatus.NOT_FOUND.value(),"이메일에 대한 인증 번호를 발급 받은 적이 없습니다.");
        } else if (redisUtil.getData(check.getEmail()).equals(authNum)) {
            return new ResponseDto(HttpStatus.OK.value(),"인증에 성공하셨습니다.");
        }else return new ResponseDto(HttpStatus.UNAUTHORIZED.value(),"인증 번호가 동일하지 않습니다.");
    }
}
