package com;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.webond.member.model.MemberVO;
import com.webond.member.model.NotificationVO;
import com.webond.member.service.MemberService;

@Controller
public class IndexController_inSpringBoot {
	@Autowired
	MemberService memberService;
	
		@GetMapping("/")
		public String myMethod1(ModelMap model) { 

			List<MemberVO> list = memberService.getALL();
			
			
			model.addAttribute("memberListData", list);
			return "index";    // --> src/main/resources/templates/index.html
		}

}