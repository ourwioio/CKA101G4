package com.webond.chat.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.webond.chat.service.ChatService;
import com.webond.member.model.MemberVO;

import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/chat")
public class ChatApiController {
	 @Autowired
	    private ChatService chatSvc;
	 
		@GetMapping("/contacts")
		public ResponseEntity<?> getChatContacts(HttpSession session) {
		    MemberVO loginUser = (MemberVO) session.getAttribute("memberVO");
		    if (loginUser == null) {
		        return ResponseEntity.status(401).body("請先登入會員");
		    }
		    
		    Integer currentUserId = loginUser.getMemberId(); 

		    List<Map<String, Object>> contactList = chatSvc.getDynamicChatContacts(currentUserId);

		    return ResponseEntity.ok(contactList);
		}

	}


