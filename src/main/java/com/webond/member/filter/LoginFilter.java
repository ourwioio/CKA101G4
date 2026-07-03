package com.webond.member.filter;


import java.io.IOException;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
	
	

	@Component
	public class LoginFilter extends OncePerRequestFilter {
		
	    /**
	     * shouldNotFilter 提供了一個乾淨的方式來排除不需要過濾的路徑
	     * 返回 true 代表「跳過過濾器」，返回 false 代表「執行過濾」
	     */
	    @Override
	    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
	        String uri = request.getRequestURI();
	        
	        // 1. 老師原本的排除：排除登入頁面，避免無限重定向
	        if (uri.endsWith("/login.html") || uri.endsWith("/login")) {
	            return true;
	        }
	        
	        // ➕ 【你新加的排除】：排除你的會員登入頁面、登入處理器與註冊功能
	        // 這些路徑必須放行，不然路人進不來註冊或登入頁面
	        if (uri.contains("/member/login") || uri.contains("/member/logincontroller") || uri.contains("/member/register") || uri.contains("/member/doRegister")) {
	            return true;
	        }
	        
	        // 2. 老師原本的受保護範圍：包含這些路徑才過濾
	        boolean isTeacherProtected = uri.contains("/protected1") || uri.contains("/protected2");
	        
	        // ➕ 【你新加的受保護範圍】：只要網址包含你的後台審核頁、或者是任何你要鎖住的會員功能
	        boolean isMyProtected = uri.contains("/memberreport/") || uri.contains("/member/listAllMember") || uri.contains("/member/reviewKyc");

	        // 🎯 只要符合老師的保護範圍，或者你的保護範圍，就返回 false（代表「不跳過」，也就是「要執行檢查」）
	        return !(isTeacherProtected || isMyProtected);
	    }

	    @Override
	    protected void doFilterInternal(HttpServletRequest request, 
	                                    HttpServletResponse response, 
	                                    FilterChain filterChain) throws ServletException, IOException {
	    	
	        String uri = request.getServletPath();
	        HttpSession session = request.getSession();

	        // 1. 檢查使用者是否登入過 (不管是老師的 User 還是你的 Member 登入成功，都會有這個 attribute)
	        Object account = session.getAttribute("account");

	        // --- account如果為 null，代表此user未登入過  ---
	        if (account == null) {
	            // session存入當前路徑，以便登入後跳轉回此路徑
	            session.setAttribute("location", uri);
	            // 重定(導)向到登入頁 (這邊統一導向老師的或你的。假設都導向你的會員登入：/member/login)
	            response.sendRedirect(request.getContextPath() + "/member/login"); 
	        } else {
	            // 已登入，繼續下一個過濾器或 Controller
	            filterChain.doFilter(request, response);
	        }
	    }
	}


