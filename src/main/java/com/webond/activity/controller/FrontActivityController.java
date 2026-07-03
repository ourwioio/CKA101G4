package com.webond.activity.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.webond.activity.model.ActivityOrderService;
import com.webond.activity.model.ActivityOrderVO;
import com.webond.activity.model.ActivityService;
import com.webond.activity.model.ActivityVO;

@Controller
public class FrontActivityController {

	@Autowired
	private ActivityService activitySvc;

	@Autowired
	private ActivityOrderService activityOrderSvc;

	// 前台活動列表
	@GetMapping("/activity/front/list")
	public String frontActivityList(Model model) {

		// 假登入會員資料
		model.addAttribute("loginMemberId", 1);
		model.addAttribute("loginMemberName", "測試會員");

		// 活動列表
		model.addAttribute("activityListData", activitySvc.getAll());

		return "front-end/activity/frontActivityList";
	}

	@GetMapping("/activity/front/detail")
	public String frontActivityDetail(@RequestParam("id") Integer activityId, Model model) {

		model.addAttribute("loginMemberId", 1);
		model.addAttribute("loginMemberName", "測試會員");

		ActivityVO activityVO = activitySvc.getOneActivity(activityId);
		model.addAttribute("activityVO", activityVO);

		return "front-end/activity/frontActivityDetail";
	}

	@GetMapping("/activity/front/myOrder")
	public String myOrder(Model model) {

		Integer loginMemberId = 1;

		model.addAttribute("loginMemberId", loginMemberId);
		model.addAttribute("loginMemberName", "測試會員");

		model.addAttribute("orderListData", activityOrderSvc.getOrdersByBuyerMemberId(loginMemberId));

		model.addAttribute("activityListData", activitySvc.getAll());

		return "front-end/activity/myActivityOrder";
	}

	@GetMapping("/activity/front/order")
	public String frontActivityOrder(@RequestParam("activityId") Integer activityId, Model model) {

		// 假登入會員
		model.addAttribute("loginMemberId", 1);
		model.addAttribute("loginMemberName", "測試會員");

		ActivityVO activityVO = activitySvc.getOneActivity(activityId);

		ActivityOrderVO orderVO = new ActivityOrderVO();
		orderVO.setActivityId(activityVO.getActivityId());
		orderVO.setBuyerMemberId(1);
		orderVO.setBookingCount(1);
		orderVO.setActivityPrice(activityVO.getActivityPrice());
		orderVO.setTotalAmount(activityVO.getActivityPrice());
		orderVO.setOrderStatus((byte) 0);

		model.addAttribute("activityVO", activityVO);
		model.addAttribute("activityOrderVO", orderVO);

		return "front-end/activity/frontActivityOrder";
	}

	@PostMapping("/activity/front/order/save")
	public String saveFrontOrder(@ModelAttribute("activityOrderVO") ActivityOrderVO orderVO) {

		// 重新計算總金額，避免使用者改 hidden 欄位
		Integer bookingCount = orderVO.getBookingCount();
		Integer activityPrice = orderVO.getActivityPrice();

		if (bookingCount == null || bookingCount < 1) {
			bookingCount = 1;
		}

		orderVO.setBookingCount(bookingCount);
		orderVO.setTotalAmount(activityPrice * bookingCount);

		// 預設訂單狀態：0 已完成
		orderVO.setOrderStatus((byte) 0);

		activityOrderSvc.addOrder(orderVO);

		return "redirect:/activity/front/myOrder";
	}
}