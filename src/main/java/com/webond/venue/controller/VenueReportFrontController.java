package com.webond.venue.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.webond.member.model.MemberVO;
import com.webond.venue.model.VenueOrderVO;
import com.webond.venue.model.VenueReportVO;
import com.webond.venue.service.VenueReportService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/venueReports")
public class VenueReportFrontController {

	private static final String ORDER_LIST_PATH = "/front/venueOrder/myVenueOrder";
	private static final String ORDER_LIST_URL = "redirect:" + ORDER_LIST_PATH;

	@Autowired
	private VenueReportService venueReportSvc;

	// ===== 檢舉表單頁：從訂單列表的「檢舉」按鈕帶 venueOrderId 進來 =====
	@GetMapping("addVenueReport")
	public String addVenueReport(@RequestParam("venueOrderId") Integer venueOrderId, HttpSession session,
			ModelMap model, RedirectAttributes redirectAttrs) {

		// 1. 登入檢查（session key "memberVO"，存整個 MemberVO）
		MemberVO loginMember = (MemberVO) session.getAttribute("memberVO");
		if (loginMember == null) {
			session.setAttribute("location", "/venueReports/addVenueReport?venueOrderId=" + venueOrderId);
			return "redirect:/member/login";
		}

		// 2. 驗證訂單存在且屬於該會員（防止竄改網址的 venueOrderId）
		VenueOrderVO venueOrderVO = venueReportSvc.getVenueOrder(venueOrderId);
		if (venueOrderVO == null || venueOrderVO.getMember() == null
				|| !venueOrderVO.getMember().getMemberId().equals(loginMember.getMemberId())) {
			redirectAttrs.addFlashAttribute("errorMsg", "查無此訂單");
			return ORDER_LIST_URL;
		}

		// 3. 防重複檢舉：每筆訂單僅能檢舉一次，不論先前的審核結果為何
		if (venueReportSvc.hasAnyReport(venueOrderId)) {
			redirectAttrs.addFlashAttribute("errorMsg", "此訂單已檢舉過，每筆訂單僅能檢舉一次");
			return ORDER_LIST_URL;
		}

		model.addAttribute("venueOrderVO", venueOrderVO);
		model.addAttribute("venueVO", venueOrderVO.getVenueVO());
		model.addAttribute("loginMember", loginMember);
		return "front-end/venueReport/addVenueReport";
	}

	// ===== 送出檢舉 =====
	@PostMapping("insert")
	public String insert(@RequestParam("venueOrderId") Integer venueOrderId,
			@RequestParam("serReportCom") String serReportCom, HttpSession session, RedirectAttributes redirectAttrs) {

		// 1. 登入檢查
		MemberVO loginMember = (MemberVO) session.getAttribute("memberVO");
		if (loginMember == null) {
			return "redirect:/member/login";
		}

		// 2. 內容驗證
		if (serReportCom == null || serReportCom.trim().isEmpty()) {
			redirectAttrs.addFlashAttribute("errorMsg", "檢舉內容不可空白");
			return "redirect:/venueReports/addVenueReport?venueOrderId=" + venueOrderId;
		}
		if (serReportCom.trim().length() > 500) {
			redirectAttrs.addFlashAttribute("errorMsg", "檢舉內容不可超過 500 字");
			return "redirect:/venueReports/addVenueReport?venueOrderId=" + venueOrderId;
		}

		// 3. 驗證訂單存在且屬於該會員
		VenueOrderVO venueOrderVO = venueReportSvc.getVenueOrder(venueOrderId);
		if (venueOrderVO == null || venueOrderVO.getMember() == null
				|| !venueOrderVO.getMember().getMemberId().equals(loginMember.getMemberId())) {
			redirectAttrs.addFlashAttribute("errorMsg", "查無此訂單");
			return ORDER_LIST_URL;
		}

		// 4. 防重複檢舉：每筆訂單僅能檢舉一次，不論先前的審核結果為何
		if (venueReportSvc.hasAnyReport(venueOrderId)) {
			redirectAttrs.addFlashAttribute("errorMsg", "此訂單已檢舉過，每筆訂單僅能檢舉一次");
			return ORDER_LIST_URL;
		}

		// 5. 寫入
		VenueReportVO venueReportVO = new VenueReportVO();
		venueReportVO.setVenueOrderId(venueOrderId);
		venueReportVO.setSerReportCom(serReportCom.trim());
		venueReportSvc.addVenueReport(venueReportVO);

		redirectAttrs.addFlashAttribute("successMsg", "檢舉已送出，我們將盡快為您處理");
		return ORDER_LIST_URL;
	}
}