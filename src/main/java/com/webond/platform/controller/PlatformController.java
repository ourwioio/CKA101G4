package com.webond.platform.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/platform")
public class PlatformController {

	@GetMapping("/index")
	public String index() {
		return "back-end/platform/index";
	}
}