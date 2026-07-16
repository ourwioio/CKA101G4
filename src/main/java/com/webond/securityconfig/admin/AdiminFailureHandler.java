package com.webond.securityconfig.admin;

import java.io.IOException;

import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class AdiminFailureHandler implements AuthenticationFailureHandler {

	@Override
	public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException exception) throws IOException, ServletException {
String errorMsg = "密碼輸入錯誤或帳號不存在"; // 設定一個安全的預設值
		
		// 🌟 核心高階技巧：直接從 request 中再次抓取前端送來的帳號密碼進行二次格式分析
		String username = request.getParameter("username");
		String password = request.getParameter("password");
		
		if (username != null) {
			username = username.trim();
		}

		// === 1. 二次檢測：判斷帳號空白與格式 ===
		if (username == null || username.isEmpty()) {
			errorMsg = "員工帳號請勿空白";
		} 
		else if (!username.matches("^[a-zA-Z0-9]{5,20}@webond\\.com$")) {
			errorMsg = "帳號格式錯誤：長度須為 5 到 20 的英文或數字，且以 @webond.com 結尾";
		} 
		// === 2. 二次檢測：判斷密碼空白與長度 ===
		else if (password == null || password.isEmpty()) {
			errorMsg = "密碼請勿空白";
		} 
		else if (password.length() < 8 || password.length() > 20) {
			errorMsg = "密碼長度必須在 8 到 20 個字元之間";
		} 
		// === 3. 格式都符合，代表是真的打錯帳密，或是被停權 ===
		else {
			String exceptionMsg = exception.getMessage();
			if (exception instanceof DisabledException || "ACCOUNT_DISABLED".equals(exceptionMsg)) {
				errorMsg = "此員工帳號已被停權或已離職，無法登入";
			} else if (exception instanceof UsernameNotFoundException || "USER_NOT_FOUND".equals(exceptionMsg)) {
				errorMsg = "員工帳號不存在";
			} else {
				errorMsg = "帳號或密碼錯誤";
			}
		}

		request.getSession().setAttribute("loginErrorMessage", errorMsg);
		response.sendRedirect(request.getContextPath() + "/admin/login?error=true");

	}

}
