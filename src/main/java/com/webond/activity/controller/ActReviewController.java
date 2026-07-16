package com.webond.activity.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.webond.activity.model.ActReviewService;
import com.webond.activity.model.ActivityOrderService;
import com.webond.activity.model.ActivityOrderVO;
import com.webond.member.model.MemberVO;

import jakarta.servlet.http.HttpSession;

@Controller
public class ActReviewController {

	@Autowired
	private ActReviewService reviewSvc;
	
	@Autowired
	private ActivityOrderService actOrdSvc;
	
	
	@GetMapping("/activity/review")
	public String showReviewPage(@RequestParam("orderId") Integer orderId, 
	                             HttpSession session,
	                             Model model) {
	    
		MemberVO loginMember = (MemberVO) session.getAttribute("memberVO");
        if (loginMember == null) {
            return "redirect:/member/login"; 
        }
		
		ActivityOrderVO order = actOrdSvc.getOneOrder(orderId);
	    if (order == null || order.getOrderStatus() != 4 ) {
	        return "redirect:/activity/front/myOrder?error=cannotReview";
	    }
	    
	    
	    model.addAttribute("orderData", order);
	    model.addAttribute("orderId", orderId);
	    return "front-end/activity/actReview"; 
	}
	

	@PostMapping("/review/submit")
	public String handleReviewSubmit(@RequestParam("orderId") Integer orderId, 
            						 @RequestParam("buyerRating") Byte buyerRating, 
            						 @RequestParam("buyerComment") String buyerComment,
									 HttpSession session, 
									 Model model) {
		

	    	MemberVO loginMember = (MemberVO) session.getAttribute("memberVO");
	        if (loginMember == null) {
	            return "redirect:/member/login"; 
	        }
	        
	        ActivityOrderVO order = actOrdSvc.getOneOrder(orderId);
	        
	        
	        // 避免重複評價
	        if (order.getBuyerRateSeller() != null) {
	            model.addAttribute("orderData", order);
	            model.addAttribute("errorMessage", "該筆訂單您已經評價過了！");
	            model.addAttribute("prevRating", buyerRating);
	            model.addAttribute("prevComment", buyerComment);
	            return "front-end/activity/myActivityOrder";
	        }
	        
	        // 欄位長度驗證
	        if (buyerComment == null || buyerComment.trim().isEmpty() || buyerComment.length() > 500) {
	            model.addAttribute("orderData", order);
	            model.addAttribute("errorMessage", "評論內容長度必須在 1 到 500 字之間。");
	            model.addAttribute("prevRating", buyerRating);
	            model.addAttribute("prevComment", buyerComment);
	            return "front-end/activity/actReview";
	        }
	        
	        try {
	            reviewSvc.saveBuyerReview(order, buyerRating, buyerComment);
	            return "redirect:/activity/review/success";
	            
	        } catch (Exception e) {
	            model.addAttribute("orderData", order);
	            model.addAttribute("errorMessage", "系統繁忙，請稍後再試。");
	            model.addAttribute("prevRating", buyerRating);
	            model.addAttribute("prevComment", buyerComment);
	            return "front-end/activity/actReview";
	        }
	
	}
	
	
    @GetMapping("/activity/review/success")
    public String showSuccessPage() {
        return "front-end/activityReport/reviewSuccess";
    }
}
