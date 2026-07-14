package com.webond.orderManagement.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.webond.orderManagement.dto.RefundPayoutDTO;
import com.webond.orderManagement.dto.RefundPayoutQueryDTO;
import com.webond.orderManagement.service.RefundPayoutService;

@Controller
@RequestMapping("/refundPayout")
public class RefundPayoutController {

	@Autowired
	private RefundPayoutService refundPayoutService;

	// 退款所有明細
	@GetMapping("/refundList")
	public String refundList(@RequestParam(defaultValue = "all") String quickRange,
			@RequestParam(required = false) String sourceType,
			@RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
			@RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate, Model model) {

		RefundPayoutQueryDTO query = new RefundPayoutQueryDTO();
		query.setQuickRange(quickRange);
		query.setSourceType(sourceType);
		if ("custom".equals(quickRange)) {
			query.setStartDate(startDate != null ? startDate.atStartOfDay() : null);
			query.setEndDate(endDate != null ? endDate.atTime(23, 59, 59) : null);
		}

		List<RefundPayoutDTO> records = refundPayoutService.findRefundList(query);

		model.addAttribute("records", records);
		model.addAttribute("selectedRange", quickRange);
		model.addAttribute("selectedSourceType", sourceType);
		return "back-end/orderManagement/refundList";
	}

	// 撥款所有明細
	@GetMapping("/payoutList")
	public String payoutList(@RequestParam(defaultValue = "all") String quickRange,
			@RequestParam(required = false) String sourceType,
			@RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
			@RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate, Model model) {

		RefundPayoutQueryDTO query = new RefundPayoutQueryDTO();
		query.setQuickRange(quickRange);
		query.setSourceType(sourceType);
		if ("custom".equals(quickRange)) {
			query.setStartDate(startDate != null ? startDate.atStartOfDay() : null);
			query.setEndDate(endDate != null ? endDate.atTime(23, 59, 59) : null);
		}

		List<RefundPayoutDTO> records = refundPayoutService.findPayoutList(query);

		model.addAttribute("records", records);
		model.addAttribute("selectedRange", quickRange);
		model.addAttribute("selectedSourceType", sourceType);
		return "back-end/orderManagement/payoutList";
	}
}
