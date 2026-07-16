package com.webond.activity.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.webond.activity.config.ActivityEmployeeSession;

import jakarta.servlet.http.HttpSession;

@Controller
public class ActivityHomeController {

	@Autowired
	private ActivityEmployeeSession employeeSession;

	@GetMapping("/activity/home")
	public String portal() {
		return "front-end/activity/activityPortal";
	}

	@GetMapping({ "/activity/admin/home", "/admin/activity/home" })
	public String adminHome(Model model, HttpSession session) {
		addLoginEmployee(model, session);
		return "front-end/activity/activityHome";
	}

	private void addLoginEmployee(Model model, HttpSession session) {
		Integer employeeId = employeeSession.getLoginEmployeeId(session);
		String employeeName = employeeSession.getLoginEmployeeName(session);

		model.addAttribute("loginEmployeeId", employeeId);
		model.addAttribute("loginEmployeeName", employeeName);
		model.addAttribute("isLoginEmployee", employeeId != null);
	}
}
