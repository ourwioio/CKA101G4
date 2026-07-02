package com.webond.venue.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.webond.venue.model.VenueOrderVO;
import com.webond.venue.model.VenueVO;
import com.webond.venue.service.VenueOrderService;
import com.webond.venue.service.VenueService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/front/venueOrder")
public class VenueOrderController {

	@Autowired
	VenueService venueService;
	
	@Autowired
	VenueOrderService venueOrderService; 
	
	@GetMapping("addVenueOrder")
	public String add(@RequestParam("venueId") Integer venueId, Model model, HttpSession session) {
	    
	    // 先驗證登入
	    Integer memberId = (Integer) session.getAttribute("loginMemberId");
	    if (memberId == null) {
	        return "redirect:/front/venue/fakeLogin";
	    }

	    // 撈場地資料顯示在預約頁面
	    VenueVO venueVO = venueService.getOneVenue(venueId);
	    model.addAttribute("venueVO", venueVO);

	    VenueOrderVO venueOrderVO = new VenueOrderVO();
	    model.addAttribute("venueOrderVO", venueOrderVO);

	    return "front-end/venue/addVenueOrder";
	}
	
	
	
}
