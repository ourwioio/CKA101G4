package com.webond.service.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.webond.member.model.MemberVO;
import com.webond.member.service.NotificationService;
import com.webond.service.dto.ServiceReviewDTO;
import com.webond.service.model.ServiceOrderVO;
import com.webond.service.service.ServiceReviewService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/orderReview")
public class ServiceReviewController {
	
	@Autowired
	ServiceReviewService serviceReviewService;
	
	@GetMapping("/write/{orderId}")
	public String showReviewForm(@PathVariable("orderId") Integer orderId, ModelMap model, HttpSession session) {
		
		MemberVO loginMember = (MemberVO) session.getAttribute("memberVO");
		if(loginMember == null) {
			return "redirect:/member/login";
		}
		
		ServiceOrderVO order = serviceReviewService.getOrderForReview(orderId, loginMember.getMemberId());
		boolean isBuyer = loginMember.getMemberId().equals(order.getBuyerMemberId());

	    ServiceReviewDTO dto = new ServiceReviewDTO();
	    dto.setOrderId(orderId); 
	    
		model.addAttribute("order", order);
		model.addAttribute("isbuyer", isBuyer);
		model.addAttribute("serviceReviewDTO", dto);
		
        return "front-end/service/serviceReview";
	}
	
	@PostMapping("/submit")
	public String submitReview(@ModelAttribute ServiceReviewDTO dto,
								BindingResult result,
								HttpSession session, 
                                RedirectAttributes redirectAttributes) {
		
		MemberVO loginMember = (MemberVO) session.getAttribute("memberVO");
		if(loginMember == null) {
			return "redirect:/member/login";
		}
		    
		if (result.hasErrors()) {
		    System.out.println("表單綁定錯誤詳細訊息：" + result.getAllErrors());
		    redirectAttributes.addFlashAttribute("errorMessage", "評分或評論內容格式不正確");
		    return "redirect:/orderReview/write/" + dto.getOrderId();
		}
		
        try {
            serviceReviewService.submitReview(dto.getOrderId(), loginMember.getMemberId(), dto);
        } catch (IllegalStateException | IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/orderReview/write/" + dto.getOrderId();
        }
        
        

        return "redirect:/orderReview/success";
		
	}
	
    
    @GetMapping("/success")
    public String reportSuccess() {    	
        return "front-end/service/serviceReviewSuccess";
    }
}
