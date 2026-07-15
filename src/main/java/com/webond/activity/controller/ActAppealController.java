package com.webond.activity.controller;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.webond.activity.model.ActRptService;
import com.webond.activity.model.ActRptVO;
import com.webond.member.model.MemberVO;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/member/activity")
public class ActAppealController {
	
	@Autowired
	private ActRptService actRptSvc;
	
	

	
	
	
	
// 申訴表單	
	@GetMapping("/appeal")
	public String showApprealPage(@RequestParam("actRptId") Integer actRptId,
								  Model model,
								  HttpSession session) {
		
		ActRptVO actRpt = actRptSvc.getOneActRpt(actRptId);
        
        MemberVO loginMem = (MemberVO) session.getAttribute("memberVO"); 
        if (loginMem == null || !actRpt.getActId().getMemberId().equals(loginMem.getMemberId())) {
            return "redirect:/member/login"; 
        }
        
        if (actRpt.getAppealTime() != null) {
            model.addAttribute("message", "本案已在申訴處理程序中。");
            return "member/appealStatus";
        }

        model.addAttribute("actRpt", actRpt);
        
        return "front-end/activityReport/appealForm"; 
	}

// 送出申訴
	@PostMapping("/appeal/submit")
	public String submitAppeal(@RequestParam("actRptId") Integer actRptId,
	                           @RequestParam("appealContent") String appealContent,
	                           @RequestParam(value = "appealImgFile", required = false) MultipartFile img,
	                           HttpSession session) throws IOException {
		
		ActRptVO actRpt = actRptSvc.getOneActRpt(actRptId);
	    MemberVO loginMem = (MemberVO) session.getAttribute("memberVO");
        if (loginMem == null || !actRpt.getActId().getMemberId().equals(loginMem.getMemberId())) {
            return "redirect:/member/login"; 
        }
	    
        byte[] imgBytes = null;
        if (img != null && !img.isEmpty()) {
            imgBytes = img.getBytes();
        }
        actRptSvc.submitAppeal(actRptId, appealContent, imgBytes);
	    
	    return "redirect:/member/activity/appeal-success"; 
	}

	
	
	
}
