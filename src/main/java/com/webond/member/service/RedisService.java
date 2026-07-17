package com.webond.member.service;

import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RedisService {

	@Autowired
	private StringRedisTemplate redisTemplate;

	// 💡 Key 前綴設計：依業務類別區分
	private static final String REGISTER_OTP_PREFIX = "auth:register:otp:";
	private static final String FORGOT_OTP_PREFIX = "auth:forgot:otp:";
	private static final String COOL_DOWN_PREFIX = "auth:cooldown:";

	// =========================================================================
	// 1. 防刷冷卻機制 (60 秒)
	// =========================================================================

	/**
	 * 檢查是否處於 60 秒發送冷卻期
	 */
	public boolean isCoolingDown(String email) {
		String key = COOL_DOWN_PREFIX + email.trim();
		return Boolean.TRUE.equals(redisTemplate.hasKey(key));
	}

	/**
	 * 設定 60 秒發送冷卻 Key
	 */
	private void setCoolDown(String email) {
		String key = COOL_DOWN_PREFIX + email.trim();
		redisTemplate.opsForValue().set(key, "1", 60, TimeUnit.SECONDS);
	}

	// =========================================================================
	// 2. 註冊 OTP 邏輯 (有效時間 10 分鐘)
	// =========================================================================

	public void saveRegisterOtp(String email, String otpCode) {
		String key = REGISTER_OTP_PREFIX + email.trim();
		redisTemplate.opsForValue().set(key, otpCode, 10, TimeUnit.MINUTES);
		setCoolDown(email);
	}

	public String getRegisterOtp(String email) {
		return redisTemplate.opsForValue().get(REGISTER_OTP_PREFIX + email.trim());
	}

	public void deleteRegisterOtp(String email) {
		redisTemplate.delete(REGISTER_OTP_PREFIX + email.trim());
	}

	// =========================================================================
	// 3. 忘記密碼 OTP 邏輯 (有效時間 10 分鐘)
	// =========================================================================

	public void saveForgotOtp(String email, String otpCode) {
		String key = FORGOT_OTP_PREFIX + email.trim();
		redisTemplate.opsForValue().set(key, otpCode, 5, TimeUnit.MINUTES);
		setCoolDown(email);
	}

	public String getForgotOtp(String email) {
		return redisTemplate.opsForValue().get(FORGOT_OTP_PREFIX + email.trim());
	}

	public void deleteForgotOtp(String email) {
		redisTemplate.delete(FORGOT_OTP_PREFIX + email.trim());
	}

	// =========================================================================
	// 4. 相容舊方法 (預設對應註冊模組，避免修改 Controller 時編譯錯誤)
	// =========================================================================

	public void saveOtp(String email, String otpCode) {
		saveRegisterOtp(email, otpCode);
	}

	public String getOtp(String email) {
		return getRegisterOtp(email);
	}

	public void deleteOtp(String email) {
		deleteRegisterOtp(email);
	}
}