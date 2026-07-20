package com.webond.member.filter;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.webond.member.model.MemberVO;
import com.webond.member.service.MemberServiceLoie;

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
 * - 本 Filter 只保護「會員」頁面清單內的網址，清單外一律放行
 *
 * 運作流程：
 * 1. shouldNotFilter()：不在保護清單內的網址回傳 true（跳過檢查）
 * 2. doFilterInternal()：
 *    a. 檢查 session.account（handleLogin 登入成功時寫入）→ 沒有就導去登入頁
 *    b. 🎯 即時狀態檢查：每次都重新查資料庫確認帳號仍是「正常(1)」，
 *       若已被停權(3)/註銷(2)，立刻銷毀 session 強制登出。
 *       搭配前端每 3 秒一次的通知輪詢（也在保護清單內），
 *       管理員停權後最慢約 3 秒該會員的 session 就會被殺掉。
 * 3. 未登入 → 把原網址存入 session.location（登入後跳回用），導向會員登入頁
 */
@Component
public class LoginFilter extends OncePerRequestFilter {

	@Autowired
	private MemberServiceLoie memberService;

	/** 需要「會員」登入的頁面 */
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

	/**
	 * 回傳 true = 跳過檢查（公開）；false = 執行檢查。
	 * 策略：黑名單制——只檢查上面清單，其餘（登入/註冊/OTP/忘記密碼、
	 * 首頁、靜態資源、他人公開頁面/頭像、其他組員的頁面、/admin/**）全部放行
	 */
	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
		String uri = request.getRequestURI();
		return !isMemberProtected(uri);
	}

	/**
	 * 判斷是否為 AJAX / JSON API 請求（例如通知輪詢 unread-count）。
	 * 這類請求未登入時：
	 * 1. 不能把網址存進 session.location（否則登入後會被導去 JSON API 頁面）
	 * 2. 不該回 302 導向登入頁（fetch 會拿到登入頁 HTML 解析失敗），直接回 401 即可
	 */
	private boolean isAjaxRequest(HttpServletRequest request) {
		String accept = request.getHeader("Accept");
		String requestedWith = request.getHeader("X-Requested-With");
		return (accept != null && accept.contains("application/json"))
				|| "XMLHttpRequest".equals(requestedWith);
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request,
									HttpServletResponse response,
									FilterChain filterChain) throws ServletException, IOException {

		String uri = request.getServletPath();
		HttpSession session = request.getSession();

		// 會員頁面：檢查會員登入身分（handleLogin 寫入）
		String account = (String) session.getAttribute("account");
		if (account == null) {
			// 🎯 AJAX 輪詢（如 /front/notification/unread-count）未登入時
			// 直接回 401，且「不寫入 location」，避免污染登入後的跳轉目標
			if (isAjaxRequest(request)) {
				response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
				return;
			}
			session.setAttribute("location", uri);
			response.sendRedirect(request.getContextPath() + "/member/login");
			return;
		}

		// =====================================================================
		// 🎯 新增：即時帳號狀態檢查（強制登出機制）
		// 已登入的會員每次存取受保護頁面時，重新到資料庫確認帳號狀態。
		// 只要不是「正常(1)」——被停權(3)、被註銷(2)、甚至資料被刪除——
		// 立刻銷毀 session 強制登出，不必等他自己登出或 session 過期。
		// =====================================================================
		MemberVO currentMember = memberService.findByEmail(account);
		if (currentMember == null
				|| currentMember.getAccountStatus() == null
				|| currentMember.getAccountStatus() != 1) {

			session.invalidate(); // 銷毀整個 session，等同強制登出

			if (isAjaxRequest(request)) {
				// 背景輪詢請求：回 401 就好（前端輪詢 3 秒打一次，
				// 所以停權後最慢約 3 秒 session 就會被殺掉）
				response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
				return;
			}
			// 一般頁面瀏覽：導回登入頁並帶上參數，讓登入頁能顯示提示訊息
			response.sendRedirect(request.getContextPath() + "/member/login?forced=1");
			return;
		}

		// 已登入且帳號狀態正常，放行給 Controller
		filterChain.doFilter(request, response);
	}
}