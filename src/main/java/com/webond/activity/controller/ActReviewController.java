package com.webond.activity.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.webond.activity.model.ActReviewService;
import com.webond.activity.model.ActivityOrderVO;
import com.webond.activity.repository.ActivityOrderRepository;
import com.webond.member.model.MemberVO;

import jakarta.servlet.http.HttpSession;

@Controller
public class ActReviewController {

	@Autowired
	private ActReviewService reviewSvc;

	@PostMapping("/submit")
	public String handleReviewSubmit(@RequestParam Integer orderId, 
									 @RequestParam byte buyerRating,
									 @RequestParam String buyerComment, 
									 HttpSession session, 
									 Model model) {
		try {

	    	MemberVO loginMember = (MemberVO) session.getAttribute("memberVO");
	        if (loginMember == null) {
	            return "redirect:/member/login"; 
	        }
	        
			reviewSvc.submitBuyerReview(orderId, buyerRating, buyerComment);

			return "redirect:/reviews/success";

		} catch (IllegalStateException | IllegalArgumentException e) {

			model.addAttribute("errorMessage", e.getMessage());

			ActivityOrderVO orderTemp = new ActivityOrderVO();
			orderTemp.setActivityOrderId(orderId);
			model.addAttribute("order", orderTemp);

			model.addAttribute("prevRating", buyerRating);
			model.addAttribute("prevComment", buyerComment);

			return "form-end/activity/actReview";

		} catch (Exception e) {
			model.addAttribute("errorMessage", "系統 busy，請稍後再試");
			return "form-end/activity/actReview";
		}
	}
	
	
	
}
