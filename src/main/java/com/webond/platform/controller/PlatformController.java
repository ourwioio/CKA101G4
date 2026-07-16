package com.webond.platform.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.webond.employee.model.EmployeeVO;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/admin/platform")
public class PlatformController {

	@GetMapping("/index")
	public String index(HttpSession session) {

		EmployeeVO loginEmp = (EmployeeVO) session.getAttribute("employeeVO");
		if (loginEmp == null) {
			return "redirect:/admin/login";
		}

		return "back-end/platform/index";
	}
}