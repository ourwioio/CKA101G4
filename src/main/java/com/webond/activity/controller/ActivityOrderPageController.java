package com.webond.activity.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.webond.activity.config.ActivityEmployeeSession;
import com.webond.activity.model.ActivityNotificationService;
import com.webond.activity.model.ActivityOrderService;
import com.webond.activity.model.ActivityOrderVO;
import com.webond.activity.model.ActivityService;
import com.webond.employee.repository.EmployeeRepository;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/activityOrder")
public class ActivityOrderPageController {

	@Autowired
	private ActivityOrderService orderSvc;

	@Autowired
	private ActivityService activitySvc;

	@Autowired
	private EmployeeRepository employeeRepo;

	@Autowired
	private ActivityEmployeeSession employeeSession;

	@Autowired
	private ActivityNotificationService activityNotificationSvc;

	@GetMapping("/listAllActivityOrder")
	public String listAllActivityOrder(@RequestParam(value = "financeStatus", required = false) String financeStatus,
			Model model, HttpSession session) {
		if (!isLoginEmployee(session)) {
			return "redirect:/activity/admin/home?loginRequired=true";
		}

		List<ActivityOrderVO> orderList = orderSvc.getAll();
		if ("pending".equals(financeStatus)) {
			orderList = orderList.stream().filter(this::isPendingFinanceOrder).collect(Collectors.toList());
		} else if ("done".equals(financeStatus)) {
			orderList = orderList.stream().filter(this::isDoneFinanceOrder).collect(Collectors.toList());
		}

		model.addAttribute("orderListData", orderList);
		model.addAttribute("activityListData", activitySvc.getAll());
		model.addAttribute("selectedFinanceStatus", financeStatus);
		addFakeEmployee(model, session);

		return "front-end/activityorder/listAllActivityOrder";
	}

	@GetMapping("/detail")
	public String orderDetail(@RequestParam("activityOrderId") Integer activityOrderId, Model model,
			HttpSession session) {
		if (!isLoginEmployee(session)) {
			return "redirect:/activity/admin/home?loginRequired=true";
		}

		ActivityOrderVO orderVO = orderSvc.getOneOrder(activityOrderId);
		if (orderVO == null) {
			return "redirect:/activityOrder/listAllActivityOrder";
		}

		model.addAttribute("orderVO", orderVO);
		model.addAttribute("activityVO", activitySvc.getOneActivity(orderVO.getActivityId()));
		addFakeEmployee(model, session);

		return "front-end/activityorder/activityOrderDetail";
	}

	@PostMapping("/confirmRefund")
	public String confirmRefund(@RequestParam("activityOrderId") Integer activityOrderId, HttpSession session) {
		Integer employeeId = employeeSession.getLoginEmployeeId(session);
		if (employeeId == null) {
			return "redirect:/activity/admin/home?loginRequired=true";
		}

		ActivityOrderVO orderVO = orderSvc.confirmRefund(activityOrderId, employeeId);
		if (orderVO == null || orderVO.getRefundStatus() == null || orderVO.getRefundStatus() != 2) {
			return "redirect:/activityOrder/detail?activityOrderId=" + activityOrderId + "&refundFailed=true";
		}

		activityNotificationSvc.notifyBuyerRefundDone(activitySvc.getOneActivity(orderVO.getActivityId()), orderVO);
		return "redirect:/activityOrder/detail?activityOrderId=" + activityOrderId + "&refundSuccess=true";
	}

	@PostMapping("/confirmRefundFromList")
	public String confirmRefundFromList(@RequestParam("activityOrderId") Integer activityOrderId,
			@RequestParam(value = "financeStatus", required = false) String financeStatus, HttpSession session) {
		Integer employeeId = employeeSession.getLoginEmployeeId(session);
		if (employeeId == null) {
			return "redirect:/activity/admin/home?loginRequired=true";
		}

		ActivityOrderVO orderVO = orderSvc.confirmRefund(activityOrderId, employeeId);
		if (orderVO != null && orderVO.getRefundStatus() != null && orderVO.getRefundStatus() == 2) {
			activityNotificationSvc.notifyBuyerRefundDone(activitySvc.getOneActivity(orderVO.getActivityId()), orderVO);
		}
		return redirectToList(financeStatus);
	}

	@PostMapping("/confirmPayout")
	public String confirmPayout(@RequestParam("activityOrderId") Integer activityOrderId, HttpSession session) {
		Integer employeeId = employeeSession.getLoginEmployeeId(session);
		if (employeeId == null) {
			return "redirect:/activity/admin/home?loginRequired=true";
		}

		ActivityOrderVO orderVO = orderSvc.confirmPayout(activityOrderId, employeeId);
		if (orderVO != null && Boolean.TRUE.equals(orderVO.getPayoutAmount())) {
			activityNotificationSvc.notifyHostPayoutDone(activitySvc.getOneActivity(orderVO.getActivityId()), orderVO);
		}
		return "redirect:/activityOrder/detail?activityOrderId=" + activityOrderId + "&payoutSuccess=true";
	}

	@PostMapping("/confirmPayoutFromList")
	public String confirmPayoutFromList(@RequestParam("activityOrderId") Integer activityOrderId,
			@RequestParam(value = "financeStatus", required = false) String financeStatus, HttpSession session) {
		Integer employeeId = employeeSession.getLoginEmployeeId(session);
		if (employeeId == null) {
			return "redirect:/activity/admin/home?loginRequired=true";
		}

		ActivityOrderVO orderVO = orderSvc.confirmPayout(activityOrderId, employeeId);
		if (orderVO != null && Boolean.TRUE.equals(orderVO.getPayoutAmount())) {
			activityNotificationSvc.notifyHostPayoutDone(activitySvc.getOneActivity(orderVO.getActivityId()), orderVO);
		}
		return redirectToList(financeStatus);
	}

	@PostMapping("/approve")
	public String approveOrder(@RequestParam("activityOrderId") Integer activityOrderId, HttpSession session) {
		if (!isLoginEmployee(session)) {
			return "redirect:/activity/admin/home?loginRequired=true";
		}
		return "redirect:/activityOrder/listAllActivityOrder?hostReviewOnly=true";
	}

	@PostMapping("/reject")
	public String rejectOrder(@RequestParam("activityOrderId") Integer activityOrderId, HttpSession session) {
		if (!isLoginEmployee(session)) {
			return "redirect:/activity/admin/home?loginRequired=true";
		}
		return "redirect:/activityOrder/listAllActivityOrder?hostReviewOnly=true";
	}

	private void addFakeEmployee(Model model, HttpSession session) {
		Integer employeeId = employeeSession.getLoginEmployeeId(session);
		String employeeName = employeeSession.getLoginEmployeeName(session);

		model.addAttribute("loginEmployeeId", employeeId);
		model.addAttribute("loginEmployeeName", employeeName);
		model.addAttribute("employeeListData", employeeRepo.findAll(Sort.by(Sort.Direction.ASC, "employeeId")));
	}

	private boolean isLoginEmployee(HttpSession session) {
		return employeeSession.isLoginEmployee(session);
	}

	private boolean isPendingFinanceOrder(ActivityOrderVO orderVO) {
		return isPendingRefund(orderVO) || isPendingPayout(orderVO);
	}

	private boolean isDoneFinanceOrder(ActivityOrderVO orderVO) {
		return isRefundDone(orderVO) || Boolean.TRUE.equals(orderVO.getPayoutAmount());
	}

	private boolean isPendingRefund(ActivityOrderVO orderVO) {
		return orderVO.getRefundStatus() != null && orderVO.getRefundStatus() == 1;
	}

	private boolean isRefundDone(ActivityOrderVO orderVO) {
		return orderVO.getRefundStatus() != null && orderVO.getRefundStatus() == 2;
	}

	private boolean isPendingPayout(ActivityOrderVO orderVO) {
		boolean noRefund = orderVO.getRefundStatus() == null || orderVO.getRefundStatus() == 0;
		boolean payableOrder = orderVO.getOrderStatus() != null
				&& (orderVO.getOrderStatus() == 0 || orderVO.getOrderStatus() == 4);
		return payableOrder && noRefund && !Boolean.TRUE.equals(orderVO.getPayoutAmount());
	}

	private String redirectToList(String financeStatus) {
		if (financeStatus == null || financeStatus.trim().isEmpty()) {
			return "redirect:/activityOrder/listAllActivityOrder";
		}

		return "redirect:/activityOrder/listAllActivityOrder?financeStatus=" + financeStatus;
	}
}
