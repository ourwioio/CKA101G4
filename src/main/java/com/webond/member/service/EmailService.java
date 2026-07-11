package com.webond.member.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    /**
     * 發送驗證碼郵件
     */
    public void sendOtpEmail(String toEmail, String otpCode) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail.trim());
        message.setSubject("【Webond 平台】會員註冊信箱驗證碼");
        message.setText("親愛的會員您好：\n\n"
                      + "您的註冊驗證碼為：[" + otpCode + "]\n\n"
                      + "該驗證碼將於 5 分鐘後失效，請勿將驗證碼提供給他人。\n"
                      + "若您未進行此操作，請忽略此郵件。");
        
        mailSender.send(message);
    }
}