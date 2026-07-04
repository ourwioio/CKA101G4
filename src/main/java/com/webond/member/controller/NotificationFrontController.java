package com.webond.member.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.webond.member.model.MemberVO;
import com.webond.member.model.NotificationVO;
import com.webond.member.service.NotificationService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/front/notification")
public class NotificationFrontController {
	@Autowired
	NotificationService notificationService;
	
	
	@GetMapping("listAllNotification")
	public String listAllNotification(ModelMap model, HttpSession session) {
		
		MemberVO loginMember = (MemberVO) session.getAttribute("memberVO");
		if(loginMember == null) {
			return "redirect:/member/login";
		}
		
		List<NotificationVO> notificationList = notificationService.getNotificationByMemberId(loginMember.getMemberId());
		model.addAttribute("notificationVO", notificationList);
		return "front-end/member/listAllNotification";
	}
	
	
	@GetMapping("getOneNotification")
	public String getOne(@RequestParam("notificationId") Integer notificationId, HttpSession session, ModelMap model) {
		
		MemberVO loginMember = (MemberVO) session.getAttribute("memberVO");
		if(loginMember == null) {
			return "redirect:/member/login";
		}
		
		NotificationVO notificationVO = notificationService.getOneNotification(notificationId);
		model.addAttribute("notificationVO",notificationVO);
		return "front-end/member/listOneNotification";
	}
}
