package com.webond.orderManagement.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/orderManagement")
public class OrderManagementController {

	@GetMapping("/index")
	public String index() {
		return "back-end/orderManagement/index";
	}
}
