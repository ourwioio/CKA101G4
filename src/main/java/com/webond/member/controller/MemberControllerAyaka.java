package com.webond.member.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.webond.member.model.MemberVO;
import com.webond.member.repository.MemberRepository;
import com.webond.member.service.MemberService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/memberPage")
public class MemberControllerAyaka {

	@Autowired
	MemberService memberService;

	@Autowired
	MemberRepository memberRepository;

	@GetMapping("/fakeLogin")
	public String fakeLogin(HttpSession session) {
		session.setAttribute("loginEmployeeId", 1001);
		return "back-end/member/empPage";
	}

	

	@GetMapping("listAllMember")
	public String listAllMember(ModelMap model) {
		List<MemberVO> list = memberService.getALL();
		model.addAttribute("memberListData", list);
		return "back-end/member/listAllMember";
	}

	@PostMapping("updateMemberStatus")
	public String update(@RequestParam ("memberId") Integer memberId, 
            @RequestParam ("accountStatus")byte accountStatus, HttpSession session, ModelMap model) {

		Integer employeeId = (Integer) session.getAttribute("loginEmployeeId");
		if (employeeId == null) {
			return "redirect:/memberPage/fakeLogin";
		}

		
		MemberVO member = new MemberVO();
		member.setMemberId(memberId);
		member.setAccountStatus(accountStatus);
		memberService.updateMemberStatus(memberId, accountStatus);
		model.addAttribute("success", "finish");

		return "redirect:/memberPage/listAllMember";

	}

}
