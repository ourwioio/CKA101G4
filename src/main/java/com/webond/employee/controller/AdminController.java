package com.webond.employee.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminController {

	//登入頁面
	@GetMapping("/login")
	public String loginPage() {
		return"/back-end/employee/login";
	}
	
	//後台主畫面
	@GetMapping("/page")
	public String adminPage() {
		return "/back-end/employee/index";
	}
	
	
	
}



