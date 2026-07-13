package com.webond.member.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

// 🟢 1. 手動引入 SLF4J Logger
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// ❌ 移除 import lombok.extern.slf4j.Slf4j; 與 @Slf4j 註解
@Service
public class EmailService {

    // 🟢 2. 手動宣告 log 物件 (與 AuthController 保持一致，絕對不報錯)
    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    @Autowired
    private JavaMailSender mailSender;

    /**
     * 發送驗證碼郵件
     * 🟢 加上 @Async 可實現非同步發送（需在 Spring Boot 主程式加上 @EnableAsync）
     */
    @Async
    public void sendOtpEmail(String toEmail, String otpCode) {
        if (toEmail == null || toEmail.isBlank()) {
            return;
        }

        String cleanEmail = toEmail.trim();

        // 🟢 1. 控制台 Log (開發測試用：Gmail 配額爆掉時可以直接在這裡看驗證碼)
        System.out.println("==========================================");
        System.out.println("🔥 【驗證碼發送】目標 Email: " + cleanEmail);
        System.out.println("🔑 【驗證碼發送】OTP 驗證碼: " + otpCode);
        System.out.println("==========================================");

        // 🟢 2. 組裝與發送郵件 (帶 try-catch 避免背景 Thread 拋出異常 Crash)
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(cleanEmail);
            message.setSubject("【Webond 平台】會員註冊信箱驗證碼");
            message.setText("親愛的會員您好：\n\n"
                          + "您的註冊驗證碼為：[" + otpCode + "]\n\n"
                          + "該驗證碼將於 5 分鐘後失效，請勿將驗證碼提供給他人。\n"
                          + "若您未進行此操作，請忽略此郵件。");

            // 寄出信件
            mailSender.send(message);
            log.info("Email 驗證碼已成功寄送至: {}", cleanEmail);

        } catch (Exception e) {
            // 🟢 3. 捕獲 SMTP 限制或網路錯誤，輸出警告，測試依然可以繼續
            System.err.println("⚠️ SMTP 郵件發送失敗 (請使用上方 Console 顯示的驗證碼進行測試)");
            log.warn("SMTP 發送失敗原因: {}", e.getMessage());
        }
    }
}