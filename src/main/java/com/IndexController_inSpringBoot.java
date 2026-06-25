package com;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class IndexController_inSpringBoot {
	
		@GetMapping("/")
		public String myMethod1() { 
			return "index";    // --> src/main/resources/templates/index.html
		}

}