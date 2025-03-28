package com.github.individualproject.service.auth;

import com.github.individualproject.service.redis.RedisUtil;
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

import java.math.BigDecimal;

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
    //습도에 대한 이메일 전송
    @Transactional
    public void sendHumidResult(String email, BigDecimal humid) {
        sender.send(createMessageByHumid(email,humid));
    }
    //습도에 대한 이메일 메시지 생성
    public MimeMessage createMessageByHumid(String email, BigDecimal humid){

        //메시지 생성
        MimeMessage mimeMessage = sender.createMimeMessage();
        try{
            mimeMessage.setFrom(senderEmail);
            mimeMessage.setRecipients(MimeMessage.RecipientType.TO,email);
            mimeMessage.setSubject("[습도 위험 상태]");
            String body = "<html>" +
                    "<body>" +
                    "<h2 style='color: #e74c3c;'>습도 위험 상태 알림</h2>" +
                    "<p style='font-size: 16px;'>현재 2시간 동안의 평균 습도가 위험 수치에 도달했습니다. 즉시 확인이 필요합니다!</p>" +
                    "<p style='font-size: 18px; color: #333333;'>현재 평균 습도: <strong>" + humid + "%</strong></p>" +
                    "<p style='font-size: 16px;'>상태: <span style='color: red; font-weight: bold;'>위험</span></p>" +
                    "<p style='font-size: 14px; color: #777777;'>이 알림은 자동으로 발송된 메시지입니다. 정확한 상태 확인을 위해 시스템에서 제공하는 자료를 확인하세요.</p>" +
                    "</body>" +
                    "</html>";
            mimeMessage.setText(body,"UTF-8", "html");

        }
        catch (MessagingException e) {
            throw new RuntimeException(e);
        }catch (Exception e){
            e.printStackTrace();
        }
        return mimeMessage;
    }
    //온도에 대한 이메일 전송
    @Transactional
    public void sendTempResult(String email, BigDecimal before, BigDecimal after,BigDecimal temp) {
        sender.send(createMessageByTemp(email,before,after,temp));
    }


    //온도에 대한 이메일 메시지 생성
    public MimeMessage createMessageByTemp(String email, BigDecimal before, BigDecimal after,BigDecimal temp){

        //메시지 생성
        MimeMessage mimeMessage = sender.createMimeMessage();
        try{
            mimeMessage.setFrom(senderEmail);
            mimeMessage.setRecipients(MimeMessage.RecipientType.TO,email);
            mimeMessage.setSubject("[온도 차이 발생]");
            String body = "<html>" +
                    "<body>" +
                    "<h2 style='color: #2980b9;'>온도 차이 알림</h2>" +
                    "<p style='font-size: 16px;'>이전 온도와 현재 온도 간에 차이가 발생했습니다. 확인이 필요합니다.</p>" +
                    "<table style='width: 100%; border: 1px solid #ddd; border-collapse: collapse;'>" +
                    "<tr>" +
                    "<th style='padding: 8px; background-color: #f2f2f2; text-align: left;'>항목</th>" +
                    "<th style='padding: 8px; background-color: #f2f2f2; text-align: left;'>값</th>" +
                    "</tr>" +
                    "<tr>" +
                    "<td style='padding: 8px;'>이전 온도</td>" +
                    "<td style='padding: 8px;'>" + before + "°C</td>" +
                    "</tr>" +
                    "<tr>" +
                    "<td style='padding: 8px;'>현재 온도</td>" +
                    "<td style='padding: 8px;'>" + after + "°C</td>" +
                    "</tr>" +
                    "<tr>" +
                    "<td style='padding: 8px; color: red;'>설정 온도 차이</td>" +
                    "<td style='padding: 8px; color: red; font-weight: bold;'>" + temp + "°C</td>" +
                    "</tr>" +
                    "</table>" +
                    "<p style='font-size: 16px;'>설정하신 온도 차이만큼 차이가 발생하여 이 메일을 전송드립니다. 주의해 주세요!</p>" +
                    "<p style='font-size: 14px; color: #777777;'>이 알림은 자동으로 발송된 메시지입니다. 정확한 상태 확인을 위해 시스템에서 제공하는 자료를 확인하세요.</p>" +
                    "</body>" +
                    "</html>";
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
