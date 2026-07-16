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
import com.webond.employee.model.EmployeeVO;
import com.webond.member.model.NotificationVO;
import com.webond.member.service.NotificationService;
import com.webond.venue.dto.VenueOrderQueryDTO;
import com.webond.venue.model.VenueOrderVO;
import com.webond.venue.model.VenueVO;
import com.webond.venue.service.VenueOrderService;
import com.webond.venue.service.VenueService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/admin/venue/venueOrder")
public class VenueOrderController {

	@Autowired
	VenueOrderService venueOrderService;

	@Autowired
	VenueService venueService;

	@Autowired
	EmpService empService;
	
	@Autowired
	NotificationService notificationService;

	@GetMapping("listAllVenueOrder")
	public String listAllVenueOrder(VenueOrderQueryDTO params, BindingResult result, Model model, HttpSession session) {

		EmployeeVO loginEmp = (EmployeeVO) session.getAttribute("employeeVO");
		if (loginEmp == null) {
			return "redirect:/admin/login";
		}
		
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
	public String listAllRefund(VenueOrderQueryDTO params, Model model, HttpSession session) {

		EmployeeVO loginEmp = (EmployeeVO) session.getAttribute("employeeVO");
		if (loginEmp == null) {
			return "redirect:/admin/login";
		}
		
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
		
		EmployeeVO loginEmp = (EmployeeVO) session.getAttribute("employeeVO");
		if (loginEmp == null) {
			return "redirect:/admin/login";
		}
		
		VenueOrderVO venueOrderVO = venueOrderService.getOneVenueOrder(venueOrderId);
		venueOrderVO.setRefundStatus((byte) 1);
		venueOrderVO.setPayoutAmount((byte) 1);
		venueOrderVO.setEmpVO(loginEmp);
		venueOrderService.updateVenueOrder(venueOrderVO);
		
		// 新增通知給買家
		NotificationVO notificationVO = new NotificationVO();
		notificationVO.setMember(venueOrderVO.getMember());
		notificationVO.setTitle("場地訂單退款通知");
		notificationVO.setContent("您的場地訂單已完成退款，退款金額為：" + venueOrderVO.getTotalAmount() + "元");
		notificationVO.setNotificationType((byte) 0);
		notificationService.addNotification(notificationVO);
		
		return "redirect:/admin/venue/venueOrder/listAllRefund";
	}
	
	@GetMapping("listAllPayout")
	public String listAllPayOut(VenueOrderQueryDTO params, Model model, HttpSession session) {
		
		EmployeeVO loginEmp = (EmployeeVO) session.getAttribute("employeeVO");
		if (loginEmp == null) {
			return "redirect:/admin/login";
		}
		
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
		
		EmployeeVO loginEmp = (EmployeeVO) session.getAttribute("employeeVO");
		if (loginEmp == null) {
			return "redirect:/admin/login";
		}
		
		VenueOrderVO venueOrderVO = venueOrderService.getOneVenueOrder(venueOrderId);
		venueOrderVO.setPayoutAmount((byte) 1);
		venueOrderVO.setEmpVO(loginEmp);
		venueOrderService.updateVenueOrder(venueOrderVO);
		
		VenueVO venueVO = venueService.getOneVenue(venueOrderVO.getVenueVO().getVenueId());
		// 新增通知給場地主
		NotificationVO notificationVO = new NotificationVO();
		notificationVO.setMember(venueVO.getMember());
		notificationVO.setTitle("場地撥款通知");
		notificationVO.setContent("您的場地訂單已完成撥款，撥款金額為：" + venueOrderVO.getTotalAmount() + "元");
		notificationVO.setNotificationType((byte) 0);
		notificationService.addNotification(notificationVO);
		return "redirect:/admin/venue/venueOrder/listAllPayout";
	}

}
