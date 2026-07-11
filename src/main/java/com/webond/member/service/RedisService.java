package com.webond.member.service;

import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RedisService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    // 定義 Key 的前綴，方便在 Redis Insight 中分類管理
    private static final String OTP_PREFIX = "auth:otp:";
    private static final String COOL_DOWN_PREFIX = "auth:cooldown:";

    /**
     * 1. 檢查是否處於 60 秒冷卻期 (防刷機制)
     */
    public boolean isCoolingDown(String email) {
        String key = COOL_DOWN_PREFIX + email.trim();
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    /**
     * 2. 存入驗證碼 (有效期限 5 分鐘) 並設定 60 秒冷卻時間
     */
    public void saveOtp(String email, String otpCode) {
        String cleanEmail = email.trim();
        String otpKey = OTP_PREFIX + cleanEmail;
        String coolDownKey = COOL_DOWN_PREFIX + cleanEmail;

        // 寫入驗證碼，TTL 設定 10 分鐘
        redisTemplate.opsForValue().set(otpKey, otpCode, 10, TimeUnit.MINUTES);

        // 寫入冷卻 Key，TTL 設定 60 秒 (值隨便設即可)
        redisTemplate.opsForValue().set(coolDownKey, "1", 60, TimeUnit.SECONDS);
    }

    /**
     * 3. 從 Redis 讀取驗證碼
     */
    public String getOtp(String email) {
        String key = OTP_PREFIX + email.trim();
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * 4. 驗證成功後刪除 Redis 中的驗證碼 (避免重複被使用)
     */
    public void deleteOtp(String email) {
        String key = OTP_PREFIX + email.trim();
        redisTemplate.delete(key);
    }
}