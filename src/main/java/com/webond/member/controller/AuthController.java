package com.webond.member.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.webond.member.service.EmailService;
import com.webond.member.service.OtpService;

@RestController
@RequestMapping("/member") // 🟢 1. 修改路徑，對齊前端 /member/send-otp
public class AuthController {

    @Autowired
    private OtpService otpService;

    @Autowired
    private EmailService emailService; // 🟢 2. 修正變數名稱

    // =========================================================================
    // 1. 發送 Email 驗證碼 API (前端點擊「取得驗證碼」時呼叫)
    // =========================================================================
    @PostMapping("/send-otp")
    public ResponseEntity<?> sendOtp(@RequestParam("email") String email) {
        if (email == null || email.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "請輸入有效的 Email！"));
        }

        String cleanEmail = email.trim();

        // A. 檢查是否在 60 秒冷卻期內
        if (!otpService.canSendOtp(cleanEmail)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(Map.of("success", false, "message", "請求過於頻繁，請於 60 秒後再試！"));
        }

        try {
            // B. 產生 6 位數驗證碼
            String otpCode = otpService.generateOtpCode();

            // C. 存入 Redis (有效期限 5 分鐘，並寫入 60 秒冷卻 Key)
            otpService.saveOtp(cleanEmail, otpCode);

            // D. 開獨立線程非同步寄信，避免 HTTP 請求被卡住
            String subject = "【Webond 平台】會員系統 - 驗證碼通知";
            String content = "您好！您的註冊驗證碼為：" + otpCode + "\n\n請於 10 分鐘內輸入完成驗證。若非本人操作請忽略此信。";
            
            new Thread(() -> emailService.sendOtpEmail(cleanEmail, otpCode)).start();

            return ResponseEntity.ok(Map.of("success", true, "message", "驗證碼已寄出，請至信箱查收！"));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "郵件發送失敗，請稍後再試。"));
        }
    }

    // =========================================================================
    // 2. 核對驗證碼 API (可供獨立驗證或二階段驗證使用)
    // =========================================================================
    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestParam("email") String email, @RequestParam("inputOtp") String inputOtp) {
        if (email == null || inputOtp == null) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "請輸入 Email 與驗證碼！"));
        }

        boolean isValid = otpService.verifyOtp(email.trim(), inputOtp.trim());

        if (isValid) {
            // 🟢 3. 驗證成功後，可選擇呼叫 deleteOtp 刪除驗證碼，防止重複使用
            // otpService.deleteOtp(email.trim()); 
            
            return ResponseEntity.ok(Map.of("success", true, "message", "驗證成功！"));
        } else {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "驗證碼錯誤或已過期，請重新取得！"));
        }
    }
}