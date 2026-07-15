package com.webond.venue.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.webond.employee.model.EmpService;
import com.webond.venue.dto.VenueOrderQueryDTO;
import com.webond.venue.model.VenueOrderVO;
import com.webond.venue.service.VenueOrderService;
import com.webond.venue.service.VenueService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/venueOrder")
public class VenueOrderController {

	@Autowired
	VenueOrderService venueOrderService;

	@Autowired
	VenueService venueService;

	@Autowired
	EmpService empService;

	@GetMapping("listAllVenueOrder")
	public String listAllVenueOrder(VenueOrderQueryDTO params, BindingResult result, Model model) {

		List<VenueOrderVO> list = venueOrderService.search(params);
		if (list.isEmpty()) {
			model.addAttribute("noDataMessage", "查無符合條件的訂單資料");
		}

		model.addAttribute("venueListData", list);
		model.addAttribute("empList", empService.getAll());
		model.addAttribute("params", params); // 讓表單上記住使用者打了什麼
		return "back-end/venue/listAllVenueOrder";
	}

	@GetMapping("getOneVenueOrder")
	public String getOneVenueOrder(@RequestParam("venueOrderId") Integer venueOrderId, Model model) {
		VenueOrderVO venueOrderVO = venueOrderService.getOneVenueOrder(venueOrderId);
		model.addAttribute("venueOrderVO", venueOrderVO);
		return "back-end/venue/getOneVenueOrder";
	}
	
	@GetMapping("listAllRefund")
	public String listAllRefund(VenueOrderQueryDTO params, Model model) {

	    // 如果使用者是第一次進來(還沒選過退款狀態),預設先看「進行中」的退款
	    if (params.getRefundStatus() == null) {
	        params.setRefundStatus((byte) 0);
	    }

	    List<VenueOrderVO> list = venueOrderService.search(params);

	    if (list.isEmpty()) {
	        model.addAttribute("noDataMessage", "查無符合條件的退款訂單");
	    }

	    model.addAttribute("allRefundData", list);
	    model.addAttribute("params", params); // 讓下拉選單記住目前選的狀態

	    return "back-end/venue/listAllRefund";
	}
	
	@GetMapping("refund")
	public String refund(@RequestParam("venueOrderId") Integer venueOrderId, 
			Model model,
			HttpSession session) {
		VenueOrderVO venueOrderVO = venueOrderService.getOneVenueOrder(venueOrderId);
		venueOrderVO.setRefundStatus((byte) 1);
		venueOrderVO.setPayoutAmount((byte) 1);
		venueOrderService.updateVenueOrder(venueOrderVO);
		return "redirect:/venueOrder/listAllRefund";
	}
	
	@GetMapping("listAllPayout")
	public String listAllPayOut(VenueOrderQueryDTO params, Model model) {
		
		// 如果使用者是第一次進來(還沒選過退款狀態),預設先看「進行中」的撥款
	    if (params.getPayoutAmount() == null) {
	        params.setPayoutAmount((byte) 0);
	    }
	    params.setOrderStatus((byte) 3);
	    List<VenueOrderVO> list = venueOrderService.search(params);

	    if (list.isEmpty()) {
	        model.addAttribute("noDataMessage", "查無符合條件的撥款款項");
	    }
		
	    model.addAttribute("listAllPayout", list);
	    model.addAttribute("params", params); // 讓下拉選單記住目前選的狀態
	    
		return "back-end/venue/listAllPayout";
	}
	
	@GetMapping("payout")
	public String payout(@RequestParam("venueOrderId") Integer venueOrderId, 
			Model model,
			HttpSession session) {
		VenueOrderVO venueOrderVO = venueOrderService.getOneVenueOrder(venueOrderId);
		venueOrderVO.setPayoutAmount((byte) 1);
		venueOrderService.updateVenueOrder(venueOrderVO);
		return "redirect:/venueOrder/listAllPayout";
	}

}
