package com.webond.activity.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.webond.activity.model.ActivityOrderService;
import com.webond.activity.model.ActivityOrderVO;
import com.webond.activity.model.ActivityService;
import com.webond.activity.model.ActivityTypeService;
import com.webond.activity.model.ActivityVO;

import jakarta.validation.Valid;

@Controller
public class FrontActivityController {

	@Autowired
	private ActivityService activitySvc;

	@Autowired
	private ActivityOrderService activityOrderSvc;

	@Autowired
	private ActivityTypeService activityTypeSvc;

	@GetMapping("/activity/front/home")
	public String frontActHome(Model model) {
		model.addAttribute("loginMemberId", 1);
		model.addAttribute("loginMemberName", "測試會員");
		return "front-end/activity/frontActHome";
	}

	@GetMapping("/activity/front/list")
	public String frontActivityList(Model model) {
		model.addAttribute("loginMemberId", 1);
		model.addAttribute("loginMemberName", "測試會員");
		model.addAttribute("activityListData", activitySvc.getAll());
		return "front-end/activity/frontActivityList";
	}

	@GetMapping("/activity/front/detail")
	public String frontActivityDetail(@RequestParam("id") Integer activityId,
			@RequestParam(value = "full", required = false) Boolean full, Model model) {

		model.addAttribute("loginMemberId", 1);
		model.addAttribute("loginMemberName", "測試會員");

		ActivityVO activityVO = activitySvc.getOneActivity(activityId);
		model.addAttribute("activityVO", activityVO);

		if (Boolean.TRUE.equals(full)) {
			model.addAttribute("fullMessage", "此活動已額滿，無法再報名！");
		}

		return "front-end/activity/frontActivityDetail";
	}

	@GetMapping("/activity/front/order")
	public String frontActivityOrder(@RequestParam("activityId") Integer activityId, Model model) {
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
		Integer bookingCount = orderVO.getBookingCount();
		Integer activityPrice = orderVO.getActivityPrice();

		if (bookingCount == null || bookingCount < 1) {
			bookingCount = 1;
		}

		orderVO.setBuyerMemberId(1);
		orderVO.setBookingCount(bookingCount);
		orderVO.setTotalAmount(activityPrice * bookingCount);
		orderVO.setOrderStatus((byte) 0);

		// 檢查是否已滿團
		if (!activitySvc.canRegister(orderVO.getActivityId(), orderVO.getBookingCount())) {

			return "redirect:/activity/front/detail?id=" + orderVO.getActivityId() + "&full=true";
		}

		activityOrderSvc.addOrder(orderVO);

		activitySvc.increaseAttendees(orderVO.getActivityId(), orderVO.getBookingCount());

		return "redirect:/activity/front/myOrder";
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

	@GetMapping("/activity/front/addHostActivity")
	public String addHostActivity(Model model) {
		ActivityVO activityVO = new ActivityVO();

		activityVO.setMemberId(1);
		activityVO.setAttendeesCount(0);
		activityVO.setActivityStatus((byte) 0);

		model.addAttribute("loginMemberId", 1);
		model.addAttribute("loginMemberName", "測試會員");
		model.addAttribute("activityVO", activityVO);
		model.addAttribute("typeListData", activityTypeSvc.getAll());

		return "front-end/activity/frontAddHostActivity";
	}

	@PostMapping("/activity/front/insertHostActivity")
	public String insertHostActivity(@Valid @ModelAttribute("activityVO") ActivityVO activityVO, BindingResult result,
			Model model) {

		activityVO.setMemberId(1);

		if (activityVO.getAttendeesCount() == null) {
			activityVO.setAttendeesCount(0);
		}

		if (activityVO.getActivityStatus() == null) {
			activityVO.setActivityStatus((byte) 0);
		}

		if (result.hasErrors()) {
			model.addAttribute("loginMemberId", 1);
			model.addAttribute("loginMemberName", "測試會員");
			model.addAttribute("typeListData", activityTypeSvc.getAll());
			return "front-end/activity/frontAddHostActivity";
		}

		activitySvc.saveActivity(activityVO);

		return "redirect:/activity/front/myHostActivity";
	}

	@GetMapping("/activity/front/myHostActivity")
	public String myHostActivity(Model model) {

		Integer loginMemberId = 1;

		model.addAttribute("loginMemberId", loginMemberId);
		model.addAttribute("loginMemberName", "測試會員");

		model.addAttribute("activityListData", activitySvc.getActivitiesByMemberId(loginMemberId));

		return "front-end/activity/myHostActivity";
	}

	@GetMapping("/activity/front/memberList")
	public String memberList(@RequestParam("activityId") Integer activityId, Model model) {

		model.addAttribute("loginMemberId", 1);
		model.addAttribute("loginMemberName", "測試會員");

		ActivityVO activityVO = activitySvc.getOneActivity(activityId);
		model.addAttribute("activityVO", activityVO);

		model.addAttribute("orderListData", activityOrderSvc.getOrdersByActivityId(activityId));

		return "front-end/activity/frontActivityMemberList";
	}
}