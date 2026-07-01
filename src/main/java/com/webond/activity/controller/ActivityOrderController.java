package com.webond.activity.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.webond.activity.model.ActivityOrderService;
import com.webond.activity.model.ActivityOrderVO;
import com.webond.activity.model.ActivityService;

@Controller
@RequestMapping("/activityOrder")
public class ActivityOrderController {

	@Autowired
	private ActivityOrderService orderService;

	@Autowired
	private ActivityService activitySvc;

	// 查詢全部
	@GetMapping("/listAllOrder")
	public String listAllOrder(Model model) {

		model.addAttribute("orderListData", orderService.getAll());

		// 活動清單(給畫面顯示活動名稱)
		model.addAttribute("activityListData", activitySvc.getAll());

		return "front-end/activityOrder/listAllActivityOrder";
	}

	// 新增
	@GetMapping("/addOrder")
	public String addOrder(Model model) {

		model.addAttribute("activityOrderVO", new ActivityOrderVO());

		model.addAttribute("activityListData", activitySvc.getAll());

		return "front-end/activityOrder/addActivityOrder";
	}

	@PostMapping("/insert")
	public String insert(@ModelAttribute ActivityOrderVO orderVO) {

		orderService.createOrder(orderVO);

		return "redirect:/activityOrder/listAllOrder";
	}

	// 修改
	@GetMapping("/updateOrder")
	public String updateOrder(@RequestParam("id") Integer orderId, Model model) {

		ActivityOrderVO orderVO = orderService.getOneOrder(orderId);

		model.addAttribute("activityOrderVO", orderVO);

		model.addAttribute("activityListData", activitySvc.getAll());

		return "front-end/activityOrder/updateActivityOrder";
	}

	@PostMapping("/update")
	public String update(@ModelAttribute ActivityOrderVO formVO) {

		ActivityOrderVO orderVO = orderService.getOneOrder(formVO.getActivityOrderId());

		orderVO.setActivityId(formVO.getActivityId());
		orderVO.setMemberId(formVO.getMemberId());
		orderVO.setOrderTotal(formVO.getOrderTotal());
		orderVO.setPaymentStatus(formVO.getPaymentStatus());

		orderService.saveOrder(orderVO);

		return "redirect:/activityOrder/listAllOrder";
	}

	// 刪除
	@PostMapping("/delete")
	public String delete(@RequestParam("activityOrderId") Integer id) {

		orderService.deleteOrder(id);

		return "redirect:/activityOrder/listAllOrder";
	}

}