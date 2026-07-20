package com.webond.securityconfig.admin;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.webond.employee.repository.EmployeeRepository;

import jakarta.servlet.http.HttpServletRequest;

@Service
public class AdminForgotPasswordService {

	private final EmployeeRepository empRepository;
	private final JavaMailSender mailSender;
	private final PasswordEncoder passwordEncoder;

	public AdminForgotPasswordService(EmployeeRepository empRepository, JavaMailSender mailSender,
			PasswordEncoder passwordEncoder) {
		this.empRepository = empRepository;
		this.mailSender = mailSender;
		this.passwordEncoder = passwordEncoder;
	}

	/**
	 * 1. 處理忘記密碼申請 (生成 Token、存入資料庫、發送 Email)
	 */
	@Transactional
	public void processForgotPassword(String empAccount, HttpServletRequest request) {
		if (empRepository.existsByEmpAccount(empAccount)) {

			com.webond.employee.model.EmployeeVO emp = empRepository.findByEmpAccount(empAccount).get();

			String token = UUID.randomUUID().toString();
			
			java.time.ZoneId taipeiZone = java.time.ZoneId.of("Asia/Taipei");
			LocalDateTime nowInTaiwan = LocalDateTime.now(taipeiZone);
			LocalDateTime expiry = nowInTaiwan.plusMinutes(15); 
			
			empRepository.updateResetTokenOnly(emp.getEmployeeId(), token, expiry);

			String scheme = request.getScheme();
			String serverName = request.getServerName();
			int serverPort = request.getServerPort();
			String contextPath = request.getContextPath();

			StringBuilder urlBuilder = new StringBuilder();
			urlBuilder.append(scheme).append("://").append(serverName);

			if (("http".equals(scheme) && serverPort != 80) || ("https".equals(scheme) && serverPort != 443)) {
				urlBuilder.append(":").append(serverPort);
			}
			urlBuilder.append(contextPath);

			String resetLink = urlBuilder.toString() + "/admin/resetPassword?token=" + token;

			// 測試用真實收信 Gmail
			String testGmail = "webond101@gmail.com";

			sendEmail(testGmail, resetLink);
		}
	}

	/**
	 * 2. 驗證 Token 是否合法且未過期
	 */
	@Transactional(readOnly = true)
	public boolean validatePasswordResetToken(String token) {
		if (token == null || token.isEmpty()) {
			return false;
		}
		// 呼叫你現有的 findByResetToken，並檢查目前時間是否在過期時間之前
		return empRepository.findByResetToken(token)
				.map(emp -> emp.getTokenExpiry() != null && emp.getTokenExpiry().isAfter(LocalDateTime.now()))
				.orElse(false);
	}

	/**
	 * 3. 執行新密碼更新，並將 Token 清除作廢
	 */
	@Transactional
	public boolean updatePassword(String token, String newPassword) {
		boolean isValid = empRepository.findByResetToken(token)
				.map(emp -> emp.getTokenExpiry() != null && emp.getTokenExpiry().isAfter(LocalDateTime.now()))
				.orElse(false);

		if (!isValid) {
			return false; 
		}

		String encryptedPassword = passwordEncoder.encode(newPassword);

		int updatedRows = empRepository.updatePasswordAndClearToken(token, encryptedPassword);

		return updatedRows > 0;
	}

	/**
	 * 內部發信輔助方法
	 */
	private void sendEmail(String toEmail, String link) {
		SimpleMailMessage message = new SimpleMailMessage();
		message.setTo(toEmail);
		message.setSubject("【Webond 系統】管理後台 - 密碼重設通知");
		message.setText("您好，系統已收到您的密碼重設請求。\n\n" + "請點擊以下連結於 15 分鐘內完成密碼重設（若超過時間連結將失效）：\n" + link + "\n\n"
				+ "如果您並未申請此操作，請忽略此郵件。");
		mailSender.send(message);
	}

}
