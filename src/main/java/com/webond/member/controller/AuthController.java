package com.webond.member.controller;

import com.webond.member.service.MailService;
import com.webond.member.service.OtpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private OtpService otpService;

    @Autowired
    private MailService mailService;

    // 1. 發送 Email 驗證碼 API
    @PostMapping("/send-otp")
    public ResponseEntity<?> sendOtp(@RequestParam String email) {
        // A. 檢查是否在 60 秒冷卻期內
        if (!otpService.canSendOtp(email)) {
            return ResponseEntity.badRequest().body("請求過於頻繁，請於 60 秒後再試！");
        }

        // B. 產生 6 位數驗證碼
        String otpCode = otpService.generateOtpCode();

        // C. 存入 Redis (有效期限 5 分鐘)
        otpService.saveOtp(email, otpCode);

        // D. 異步或直接呼叫 MailService 寄信
        String subject = "Webond 會員系統 - 驗證碼通知";
        String content = "您好！您的驗證碼為：" + otpCode + "\n請於 5 分鐘內輸入完成驗證。若非本人操作請忽略此信。";
        
        // 為了不讓寄信卡住 HTTP 請求，可以開線程執行或直接呼叫
        new Thread(() -> mailService.sendMail(email, subject, content)).start();

        return ResponseEntity.ok(Map.of("success", true, "message", "驗證碼已寄出，請至信箱查收！"));
    }

    // 2. 核對驗證碼 API (註冊/登入/重設密碼時呼叫)
    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestParam String email, @RequestParam String inputOtp) {
        boolean isValid = otpService.verifyOtp(email, inputOtp);

        if (isValid) {
            return ResponseEntity.ok(Map.of("success", true, "message", "驗證成功！"));
        } else {
            return ResponseEntity.badRequest().body("驗證碼錯誤或已過期，請重新取得！");
        }
    }
}