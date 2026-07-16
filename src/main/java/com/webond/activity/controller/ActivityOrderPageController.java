package com.webond.activity.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
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

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping({ "/activityOrder", "/admin/activity/order" })
public class ActivityOrderPageController {

	private static final String ADMIN_ACTIVITY_HOME = "/admin/activity/home";
	private static final String ADMIN_ORDER_LIST = "/admin/activity/order/listAllActivityOrder";
	private static final String ADMIN_ORDER_DETAIL = "/admin/activity/order/detail";
	private static final String ADMIN_ORDER_FINANCE = "/admin/activity/order/finance";

	@Autowired
	private ActivityOrderService orderSvc;

	@Autowired
	private ActivityService activitySvc;

	@Autowired
	private ActivityEmployeeSession employeeSession;

	@Autowired
	private ActivityNotificationService activityNotificationSvc;

	@GetMapping("/listAllActivityOrder")
	public String listAllActivityOrder(Model model, HttpSession session) {
		if (!isLoginEmployee(session)) {
			return "redirect:" + ADMIN_ACTIVITY_HOME + "?loginRequired=true";
		}

		model.addAttribute("orderListData", orderSvc.getAll());
		model.addAttribute("activityListData", activitySvc.getAll());
		addLoginEmployee(model, session);

		return "front-end/activityorder/listAllActivityOrder";
	}

	@GetMapping("/finance")
	public String financeOrders(@RequestParam(value = "financeStatus", required = false) String financeStatus,
			@RequestParam(value = "orderStatus", required = false) String orderStatus, Model model, HttpSession session) {
		if (!isLoginEmployee(session)) {
			return "redirect:" + ADMIN_ACTIVITY_HOME + "?loginRequired=true";
		}

		List<ActivityOrderVO> orderList = orderSvc.getAll().stream()
				.filter(orderVO -> isFinanceRelatedOrder(orderVO))
				.collect(Collectors.toList());

		if ("pending".equals(financeStatus)) {
			orderList = orderList.stream().filter(this::isPendingFinanceOrder).collect(Collectors.toList());
		} else if ("done".equals(financeStatus)) {
			orderList = orderList.stream().filter(this::isDoneFinanceOrder).collect(Collectors.toList());
		}

		if ("cancelled".equals(orderStatus)) {
			orderList = orderList.stream().filter(orderVO -> orderVO.getOrderStatus() != null && orderVO.getOrderStatus() == 1)
					.collect(Collectors.toList());
		} else if ("completed".equals(orderStatus)) {
			orderList = orderList.stream().filter(orderVO -> orderVO.getOrderStatus() != null
					&& (orderVO.getOrderStatus() == 0 || orderVO.getOrderStatus() == 4))
					.collect(Collectors.toList());
		}

		model.addAttribute("orderListData", orderList);
		model.addAttribute("activityListData", activitySvc.getAll());
		model.addAttribute("selectedFinanceStatus", financeStatus);
		model.addAttribute("selectedOrderStatus", orderStatus);
		addLoginEmployee(model, session);

		return "front-end/activityorder/activityOrderFinance";
	}

	@GetMapping("/detail")
	public String orderDetail(@RequestParam("activityOrderId") Integer activityOrderId, Model model,
			HttpSession session) {
		if (!isLoginEmployee(session)) {
			return "redirect:" + ADMIN_ACTIVITY_HOME + "?loginRequired=true";
		}

		ActivityOrderVO orderVO = orderSvc.getOneOrder(activityOrderId);
		if (orderVO == null) {
			return "redirect:" + ADMIN_ORDER_LIST;
		}

		model.addAttribute("orderVO", orderVO);
		model.addAttribute("activityVO", activitySvc.getOneActivity(orderVO.getActivityId()));
		addLoginEmployee(model, session);

		return "front-end/activityorder/activityOrderDetail";
	}

	@PostMapping("/confirmRefund")
	public String confirmRefund(@RequestParam("activityOrderId") Integer activityOrderId, HttpSession session) {
		Integer employeeId = employeeSession.getLoginEmployeeId(session);
		if (employeeId == null) {
			return "redirect:" + ADMIN_ACTIVITY_HOME + "?loginRequired=true";
		}

		ActivityOrderVO orderVO = orderSvc.confirmRefund(activityOrderId, employeeId);
		if (orderVO == null || orderVO.getRefundStatus() == null || orderVO.getRefundStatus() != 2) {
			return "redirect:" + ADMIN_ORDER_DETAIL + "?activityOrderId=" + activityOrderId + "&refundFailed=true";
		}

		activityNotificationSvc.notifyBuyerRefundDone(activitySvc.getOneActivity(orderVO.getActivityId()), orderVO);
		return "redirect:" + ADMIN_ORDER_DETAIL + "?activityOrderId=" + activityOrderId + "&refundSuccess=true";
	}

	@PostMapping("/confirmRefundFromList")
	public String confirmRefundFromList(@RequestParam("activityOrderId") Integer activityOrderId,
			@RequestParam(value = "financeStatus", required = false) String financeStatus,
			@RequestParam(value = "orderStatus", required = false) String orderStatus, HttpSession session) {
		Integer employeeId = employeeSession.getLoginEmployeeId(session);
		if (employeeId == null) {
			return "redirect:" + ADMIN_ACTIVITY_HOME + "?loginRequired=true";
		}

		ActivityOrderVO orderVO = orderSvc.confirmRefund(activityOrderId, employeeId);
		if (orderVO != null && orderVO.getRefundStatus() != null && orderVO.getRefundStatus() == 2) {
			activityNotificationSvc.notifyBuyerRefundDone(activitySvc.getOneActivity(orderVO.getActivityId()), orderVO);
		}
		return redirectToFinance(financeStatus, orderStatus);
	}

	@PostMapping("/confirmPayout")
	public String confirmPayout(@RequestParam("activityOrderId") Integer activityOrderId, HttpSession session) {
		Integer employeeId = employeeSession.getLoginEmployeeId(session);
		if (employeeId == null) {
			return "redirect:" + ADMIN_ACTIVITY_HOME + "?loginRequired=true";
		}

		ActivityOrderVO orderVO = orderSvc.confirmPayout(activityOrderId, employeeId);
		if (orderVO != null && Boolean.TRUE.equals(orderVO.getPayoutAmount())) {
			activityNotificationSvc.notifyHostPayoutDone(activitySvc.getOneActivity(orderVO.getActivityId()), orderVO);
		}
		return "redirect:" + ADMIN_ORDER_DETAIL + "?activityOrderId=" + activityOrderId + "&payoutSuccess=true";
	}

	@PostMapping("/confirmPayoutFromList")
	public String confirmPayoutFromList(@RequestParam("activityOrderId") Integer activityOrderId,
			@RequestParam(value = "financeStatus", required = false) String financeStatus,
			@RequestParam(value = "orderStatus", required = false) String orderStatus, HttpSession session) {
		Integer employeeId = employeeSession.getLoginEmployeeId(session);
		if (employeeId == null) {
			return "redirect:" + ADMIN_ACTIVITY_HOME + "?loginRequired=true";
		}

		ActivityOrderVO orderVO = orderSvc.confirmPayout(activityOrderId, employeeId);
		if (orderVO != null && Boolean.TRUE.equals(orderVO.getPayoutAmount())) {
			activityNotificationSvc.notifyHostPayoutDone(activitySvc.getOneActivity(orderVO.getActivityId()), orderVO);
		}
		return redirectToFinance(financeStatus, orderStatus);
	}

	@PostMapping("/approve")
	public String approveOrder(@RequestParam("activityOrderId") Integer activityOrderId, HttpSession session) {
		if (!isLoginEmployee(session)) {
			return "redirect:" + ADMIN_ACTIVITY_HOME + "?loginRequired=true";
		}
		return "redirect:" + ADMIN_ORDER_LIST + "?hostReviewOnly=true";
	}

	@PostMapping("/reject")
	public String rejectOrder(@RequestParam("activityOrderId") Integer activityOrderId, HttpSession session) {
		if (!isLoginEmployee(session)) {
			return "redirect:" + ADMIN_ACTIVITY_HOME + "?loginRequired=true";
		}
		return "redirect:" + ADMIN_ORDER_LIST + "?hostReviewOnly=true";
	}

	private void addLoginEmployee(Model model, HttpSession session) {
		Integer employeeId = employeeSession.getLoginEmployeeId(session);
		String employeeName = employeeSession.getLoginEmployeeName(session);

		model.addAttribute("loginEmployeeId", employeeId);
		model.addAttribute("loginEmployeeName", employeeName);
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
		return hasPositiveAmount(orderVO) && orderVO.getRefundStatus() != null && orderVO.getRefundStatus() == 1;
	}

	private boolean isRefundDone(ActivityOrderVO orderVO) {
		return hasPositiveAmount(orderVO) && orderVO.getRefundStatus() != null && orderVO.getRefundStatus() == 2;
	}

	private boolean isPendingPayout(ActivityOrderVO orderVO) {
		boolean noRefund = orderVO.getRefundStatus() == null || orderVO.getRefundStatus() == 0;
		boolean payableOrder = orderVO.getOrderStatus() != null
				&& (orderVO.getOrderStatus() == 0 || orderVO.getOrderStatus() == 4);
		return hasPositiveAmount(orderVO) && payableOrder && noRefund && !Boolean.TRUE.equals(orderVO.getPayoutAmount());
	}

	private boolean isFinanceRelatedOrder(ActivityOrderVO orderVO) {
		return isPendingFinanceOrder(orderVO) || isDoneFinanceOrder(orderVO)
				|| (orderVO.getOrderStatus() != null && orderVO.getOrderStatus() == 1);
	}

	private boolean hasPositiveAmount(ActivityOrderVO orderVO) {
		return orderVO != null && orderVO.getTotalAmount() != null && orderVO.getTotalAmount() > 0;
	}

	private String redirectToFinance(String financeStatus, String orderStatus) {
		StringBuilder redirectUrl = new StringBuilder("redirect:" + ADMIN_ORDER_FINANCE);
		boolean hasParam = false;

		if (financeStatus != null && !financeStatus.trim().isEmpty()) {
			redirectUrl.append("?financeStatus=").append(financeStatus);
			hasParam = true;
		}

		if (orderStatus != null && !orderStatus.trim().isEmpty()) {
			redirectUrl.append(hasParam ? "&" : "?").append("orderStatus=").append(orderStatus);
		}

		return redirectUrl.toString();
	}
}
