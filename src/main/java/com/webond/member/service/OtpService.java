package com.webond.member.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
public class OtpService {

	@Autowired
	private StringRedisTemplate redisTemplate;

	// 1. 產生 6 位數隨機數字驗證碼
	public String generateOtpCode() {
		Random random = new Random();
		int number = random.nextInt(900000) + 100000;
		return String.valueOf(number);
	}

	// 2. 檢查發送頻率 (防刷：60 秒內只能發送一次)
	public boolean canSendOtp(String email) {
		String lockKey = "auth:cooldown:" + email;
		// setIfAbsent 等同於 Redis 的 SETNX，如果 Key 不存在才會設置成功
		Boolean isFirstTime = redisTemplate.opsForValue().setIfAbsent(lockKey, "1", 60, TimeUnit.SECONDS);
		return Boolean.TRUE.equals(isFirstTime);
	}

	// 3. 儲存驗證碼至 Redis (設定 10 分鐘自動過期)
	public void saveOtp(String email, String otpCode) {
		String otpKey = "auth:otp:" + email;
		// 寫入 Redis 並設定 TTL 為 10 分鐘
		redisTemplate.opsForValue().set(otpKey, otpCode, 10, TimeUnit.MINUTES);
	}

	// 4. 驗證使用者輸入的驗證碼
	public boolean verifyOtp(String email, String inputOtp) {
		String otpKey = "auth:otp:" + email;
		String realOtp = redisTemplate.opsForValue().get(otpKey);

		if (realOtp == null) {
			// 驗證碼不存在或已過期
			return false;
		}

		if (realOtp.equals(inputOtp)) {
			// 驗證成功後，立刻刪除 Redis 裡的驗證碼 (防止二次使用)
			redisTemplate.delete(otpKey);
			return true;
		}

		return false;
	}
}
