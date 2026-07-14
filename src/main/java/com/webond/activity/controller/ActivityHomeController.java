package com.webond.activity.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.webond.activity.config.ActivityEmployeeSession;
import com.webond.employee.repository.EmployeeRepository;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/activity")
public class ActivityHomeController {

	@Autowired
	private EmployeeRepository employeeRepo;

	@Autowired
	private ActivityEmployeeSession employeeSession;

	@GetMapping("/home")
	public String portal() {
		return "front-end/activity/activityPortal";
	}

	@GetMapping("/admin/home")
	public String adminHome(Model model, HttpSession session) {
		addFakeEmployee(model, session);
		return "front-end/activity/activityHome";
	}

	@PostMapping("/admin/fakeLogin")
	public String switchFakeEmployee(@RequestParam(value = "employeeId", required = false) Integer employeeId,
			HttpSession session) {
		employeeSession.switchActivityTestEmployee(session, employeeId);
		return "redirect:/activity/admin/home";
	}

	private void addFakeEmployee(Model model, HttpSession session) {
		Integer employeeId = employeeSession.getLoginEmployeeId(session);
		String employeeName = employeeSession.getLoginEmployeeName(session);

		model.addAttribute("loginEmployeeId", employeeId);
		model.addAttribute("loginEmployeeName", employeeName);
		model.addAttribute("isLoginEmployee", employeeId != null);
		model.addAttribute("employeeListData", employeeRepo.findAll(Sort.by(Sort.Direction.ASC, "employeeId")));
	}
}
