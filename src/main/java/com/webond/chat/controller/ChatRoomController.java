package com.webond.chat.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.webond.chat.service.ChatService;
import com.webond.member.model.MemberVO;
import com.webond.member.service.MemberService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/member")
public class ChatRoomController {
	
	@Autowired
	private MemberService memberSvc; 
	
	@Autowired
	private ChatService chatSvc; 
	


	
	@GetMapping("/chat")
	public String chat(
			HttpSession session,
			@RequestParam(value = "me", required = false) Integer meId,
			@RequestParam("to") Integer toId,
			Model model) {
		
		MemberVO loginUser = (MemberVO) session.getAttribute("memberVO");
		
		if(meId ==null) {
			if(loginUser == null) {
			return "redirect:/member/login";
			}
			meId = loginUser.getMemberId();		}
		
		
		MemberVO myVO = memberSvc.getOneMember(meId);
		MemberVO targetVO = memberSvc.getOneMember(toId);
		
		
		model.addAttribute("currentUserId", myVO.getMemberId());
		model.addAttribute("currentUserName", myVO.getRealName());
		
		model.addAttribute("targetUserId", targetVO.getMemberId());
		model.addAttribute("targetName", targetVO.getRealName());
		
		List<Map<String, Object>> dynamicList = chatSvc.getDynamicChatContacts(myVO.getMemberId());
		model.addAttribute("memList", dynamicList);

		return "front-end/chat/chatRoom";
		
	}
	
	
	@GetMapping("/chatRoom")
	public String chatRoom(
			HttpSession session,
			@RequestParam(value = "to", required = false) Integer toId,
			Model model) {
		
		MemberVO loginUser = (MemberVO) session.getAttribute("memberVO");
		if(loginUser == null) {
			return "redirect:/member/login"; 
		}
		
		model.addAttribute("currentUserId", loginUser.getMemberId());
		model.addAttribute("currentUserName", loginUser.getRealName());
		
		
		List<Map<String, Object>> dynamicList = chatSvc.getDynamicChatContacts(loginUser.getMemberId());
		model.addAttribute("memList", dynamicList);
		
		if(toId != null) {
			MemberVO targetMem = memberSvc.getOneMember(toId);
			
			model.addAttribute("targetUserId", targetMem.getMemberId());
			model.addAttribute("targetName", targetMem.getRealName());
		}
		
        
        return "front-end/chat/chatRoom";
		
		
	}
	
	

	

}
