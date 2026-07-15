package com.webond.venue.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
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

	// TODO 改成貴組前台場地訂單列表的實際路徑
	private static final String ORDER_LIST_PATH = "/front/venueOrder/list";
	private static final String ORDER_LIST_URL = "redirect:" + ORDER_LIST_PATH;

	@Autowired
	private VenueReportService venueReportSvc;

	@PostMapping("insert")
	public String insert(@RequestParam("venueOrderId") Integer venueOrderId,
			@RequestParam("serReportCom") String serReportCom, HttpSession session, RedirectAttributes redirectAttrs) {

		// 1. 登入檢查（session key "memberVO"，存整個 MemberVO）
		MemberVO loginMember = (MemberVO) session.getAttribute("memberVO");
		if (loginMember == null) {
			session.setAttribute("location", ORDER_LIST_PATH); // 配合 LoginFilter 登入後跳回
			return "redirect:/member/login";
		}

		// 2. 內容驗證
		if (serReportCom == null || serReportCom.trim().isEmpty()) {
			redirectAttrs.addFlashAttribute("errorMsg", "檢舉內容不可空白");
			return ORDER_LIST_URL;
		}
		if (serReportCom.trim().length() > 500) {
			redirectAttrs.addFlashAttribute("errorMsg", "檢舉內容不可超過 500 字");
			return ORDER_LIST_URL;
		}

		// 3. 驗證訂單存在且屬於該會員（防止竄改 venueOrderId）
		VenueOrderVO venueOrderVO = venueReportSvc.getVenueOrder(venueOrderId);
		if (venueOrderVO == null || venueOrderVO.getMember() == null
				|| !venueOrderVO.getMember().getMemberId().equals(loginMember.getMemberId())) {
			redirectAttrs.addFlashAttribute("errorMsg", "查無此訂單");
			return ORDER_LIST_URL;
		}

		// 4. 防重複檢舉
		if (venueReportSvc.hasReviewingReport(venueOrderId)) {
			redirectAttrs.addFlashAttribute("errorMsg", "此訂單已有檢舉正在審核中，請等待處理結果");
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
