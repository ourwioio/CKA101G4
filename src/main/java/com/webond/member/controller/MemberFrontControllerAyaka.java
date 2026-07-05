package com.webond.member.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.SessionAttribute;

import com.webond.member.model.MemberVO;
import com.webond.member.service.MemberService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/front/memberPage")
public class MemberFrontControllerAyaka {
	@Autowired
	MemberService memberService;
	
	
	//會員個人頁面
	@GetMapping("{memberId}")
	public String getOneMember(@PathVariable Integer memberId,ModelMap model, HttpSession session) {
		
		MemberVO loginMember = (MemberVO) session.getAttribute("memberVO");
		if(loginMember == null) {
			return "redirect:/member/login";
		}

		MemberVO memberVO = memberService.getOneMember(memberId);
		model.addAttribute("memberVO", memberVO);
		return "front-end/member/profile";
	}
	
	//會員編輯資料
	@GetMapping("edit")
	public String editProfile(@SessionAttribute Integer memberId, ModelMap model) {
		MemberVO memberVO = memberService.getOneMember(memberId);
		model.addAttribute(memberVO);
		return "front-end/member/edit";
	}
	
	@PostMapping("update")
	public String updateProfile(@Valid MemberVO memberVO,BindingResult result,ModelMap model) {
		if(result.hasErrors()) {
			return "front/member/edit";
		}
		memberService.updateMember(memberVO);
		model.addAttribute("success", "finish");
		
		return "redirect:/memberPage/" + memberVO.getMemberId();
		
	}
	
	

}
