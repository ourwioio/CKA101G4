package com.webond.activity.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.webond.activity.config.ActivityEmployeeSession;
import com.webond.activity.model.ActivityOrderService;
import com.webond.activity.model.ActivityService;
import com.webond.activity.model.ActivityTypeService;
import com.webond.activity.model.ActivityVO;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/activity")
public class ActivityController {

	@Autowired
	private ActivityService activitySvc;

	@Autowired
	private ActivityTypeService activityTypeSvc;

	@Autowired
	private ActivityOrderService activityOrderSvc;

	@Autowired
	private ActivityEmployeeSession employeeSession;

	@GetMapping("/listAllActivity")
	public String listAllActivity(Model model, HttpSession session) {
		if (!isLoginEmployee(session)) {
			return "redirect:/activity/admin/home?loginRequired=true";
		}

		model.addAttribute("activityListData", activitySvc.getAll());
		model.addAttribute("typeListData", activityTypeSvc.getAll());
		return "front-end/activity/listAllActivity";
	}

	@PostMapping("/takeDown")
	public String takeDownActivity(@RequestParam("activityId") Integer activityId, HttpSession session) {
		if (!isLoginEmployee(session)) {
			return "redirect:/activity/admin/home?loginRequired=true";
		}

		ActivityVO activityVO = activitySvc.getOneActivity(activityId);

		if (activityVO != null) {
			activityVO.setActivityStatus((byte) 2);
			activitySvc.saveActivity(activityVO);
			activityOrderSvc.rejectPendingOrdersByActivity(activityId, null);
		}

		return "redirect:/activity/listAllActivity";
	}

	@GetMapping("/addActivity")
	public String addActivityDisabled(HttpSession session) {
		if (!isLoginEmployee(session)) {
			return "redirect:/activity/admin/home?loginRequired=true";
		}
		return "redirect:/activity/listAllActivity";
	}

	@PostMapping("/insert")
	public String insertDisabled(HttpSession session) {
		if (!isLoginEmployee(session)) {
			return "redirect:/activity/admin/home?loginRequired=true";
		}
		return "redirect:/activity/listAllActivity";
	}

	@GetMapping("/updateActivity")
	public String updateActivityDisabled(HttpSession session) {
		if (!isLoginEmployee(session)) {
			return "redirect:/activity/admin/home?loginRequired=true";
		}
		return "redirect:/activity/listAllActivity";
	}

	@PostMapping("/update")
	public String updateDisabled(HttpSession session) {
		if (!isLoginEmployee(session)) {
			return "redirect:/activity/admin/home?loginRequired=true";
		}
		return "redirect:/activity/listAllActivity";
	}

	@PostMapping("/deleteActivity")
	public String deleteActivityDisabled(HttpSession session) {
		if (!isLoginEmployee(session)) {
			return "redirect:/activity/admin/home?loginRequired=true";
		}
		return "redirect:/activity/listAllActivity";
	}

	@GetMapping("/activityHome")
	public String activityHome() {
		return "redirect:/activity/admin/home";
	}

	private boolean isLoginEmployee(HttpSession session) {
		return employeeSession.isLoginEmployee(session);
	}
}
