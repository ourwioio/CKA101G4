package com.webond.member.filter;

import java.io.IOException;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * 登入門禁 Filter（會員模組專用）
 *
 * 權責劃分：
 * - /admin/** 由 AdminSecurityConfig（Spring Security）保護，本 Filter 不碰
 * - 本 Filter 只保護下面兩類清單內的網址，清單外一律放行，
 *   避免誤擋其他組員的公開頁面
 *
 * 運作流程：
 * 1. shouldNotFilter()：不在保護清單內的網址回傳 true（跳過檢查）
 * 2. doFilterInternal()：依網址類型檢查對應的登入身分
 *    - 會員頁面 → session.account（handleLogin 登入成功時寫入）
 *    - 員工後台 → session.employeeVO（AdminLoginSuccessHandler 寫入）
 * 3. 未登入 → 把原網址存入 session.location（登入後跳回用），導向對應登入頁
 */
@Component
public class LoginFilter extends OncePerRequestFilter {

	/** A. 需要「會員」登入的頁面 */
	private boolean isMemberProtected(String uri) {
		return uri.contains("/member/memberSelect_page")
				|| uri.contains("/front/memberPage/my")
				|| uri.contains("/front/memberPage/edit")
				|| uri.contains("/front/memberPage/update")
				|| uri.contains("/front/memberPage/changePassword")
				|| uri.contains("/front/memberPage/deletePic")
				|| uri.contains("/front/memberPage/delete")
				|| uri.contains("/front/notification/")
				|| uri.contains("/backend/memberreport/addReport");
	}

	/** B. 需要「員工」登入的後台頁面（不在 /admin/** 底下，Spring Security 管不到） */
	private boolean isEmployeeProtected(String uri) {
		return uri.contains("/member/back-end/memberList")
				|| uri.contains("/member/back-end/toggleStatus")
				|| uri.contains("/member/back-end/displayImage")
				|| uri.contains("/backend/memberreport/manageReport")
				|| uri.contains("/backend/memberreport/updateReportStatus");
	}

	/**
	 * 回傳 true = 跳過檢查（公開）；false = 執行檢查。
	 * 策略：黑名單制——只檢查上面兩份清單，其餘（登入/註冊/OTP/忘記密碼、
	 * 首頁、靜態資源、他人公開頁面/頭像、其他組員的頁面）全部放行
	 */
	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
		String uri = request.getRequestURI();
		return !(isMemberProtected(uri) || isEmployeeProtected(uri));
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request,
									HttpServletResponse response,
									FilterChain filterChain) throws ServletException, IOException {

		String uri = request.getServletPath();
		HttpSession session = request.getSession();

		if (isEmployeeProtected(uri)) {
			// 員工後台：檢查員工登入身分（同學的 AdminLoginSuccessHandler 寫入）
			if (session.getAttribute("employeeVO") == null) {
				session.setAttribute("location", uri);
				response.sendRedirect(request.getContextPath() + "/admin/login");
				return;
			}
		} else {
			// 會員頁面：檢查會員登入身分（handleLogin 寫入）
			if (session.getAttribute("account") == null) {
				session.setAttribute("location", uri);
				response.sendRedirect(request.getContextPath() + "/member/login");
				return;
			}
		}

		// 已登入，放行給 Controller
		filterChain.doFilter(request, response);
	}
}