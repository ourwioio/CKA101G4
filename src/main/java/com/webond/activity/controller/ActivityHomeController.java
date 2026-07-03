package com.webond.activity.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/activity")
public class ActivityHomeController {

	// 模組入口
	@GetMapping("/home")
	public String portal() {
		return "front-end/activity/activityPortal";
	}

	// 管理後台首頁
	@GetMapping("/admin/home")
	public String adminHome() {
		return "front-end/activity/activityHome";
	}

}