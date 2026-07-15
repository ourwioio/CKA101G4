package com.webond.member.controller;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
@RequestMapping("/member")
public class AuthController {

    @Autowired
    private OtpService otpService;

    @Autowired
    private EmailService emailService;
    
 // 🟢 手動宣告 log 物件 (紅線保證立刻消失)
    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    // =========================================================================
    // 1. 發送 Email 驗證碼 API
    // =========================================================================
    @PostMapping("/send-otp")
    public ResponseEntity<?> sendOtp(@RequestParam("email") String email) {
        if (email == null || email.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "請輸入有效的 Email！"));
        }

        String cleanEmail = email.trim();

        // A. 檢查是否在 60 秒冷卻期內（防刷）
        if (!otpService.canSendOtp(cleanEmail)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(Map.of("success", false, "message", "請求過於頻繁，請於 60 秒後再試！"));
        }

        try {
            // B. 產生並寫入 Redis (包含 OTP 與 60s 冷卻 Key)
            String otpCode = otpService.generateOtpCode();
            otpService.saveOtp(cleanEmail, otpCode);

            // C. 呼叫 EmailService 發送驗證碼
            // 🟢 2. 移除 new Thread，由 EmailService 的 @Async 或內建邏輯處理非同步
            emailService.sendOtpEmail(cleanEmail, otpCode);

            return ResponseEntity.ok(Map.of("success", true, "message", "驗證碼已寄出，請至信箱查收！"));

        } catch (Exception e) {
            // 🟢 3. 此時 @Slf4j 注入的 log 物件將正常運作，不再紅字報錯！
            log.error("發送 OTP 驗證碼失敗, Email: {}", cleanEmail, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "系統產生驗證碼失敗，請稍後再試。"));
        }
    }

    // =========================================================================
    // 2. 核對驗證碼 API
    // =========================================================================
    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(
            @RequestParam("email") String email, 
            @RequestParam("inputOtp") String inputOtp) {

        if (email == null || email.isBlank() || inputOtp == null || inputOtp.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "請輸入 Email 與驗證碼！"));
        }

        String cleanEmail = email.trim();
        String cleanOtp = inputOtp.trim();

        boolean isValid = otpService.verifyOtp(cleanEmail, cleanOtp);

        if (isValid) {
            // 🟢 4. 驗證成功後立即刪除 Redis 中的驗證碼，防止二次重用
            otpService.deleteOtp(cleanEmail);

            // 🟢 5. 標記此 Email 已通過驗證，供 doRegister 後端把關使用（防止繞過前端直接註冊）
            otpService.markVerified(cleanEmail);

            return ResponseEntity.ok(Map.of("success", true, "message", "驗證成功！"));
        } else {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "驗證碼錯誤或已過期，請重新取得！"));
        }
    }
}