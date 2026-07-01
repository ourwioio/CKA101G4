package com.webond.venue.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.webond.venue.model.VenueOrderVO;
import com.webond.venue.service.VenueOrderService;

@Controller
@RequestMapping("/front/venueOrder")
public class VenueOrderController {

	@Autowired
	VenueOrderService venueOrderService; 
	
	@GetMapping("addVenueOrder")
	public String addOrder(Model model) {
		VenueOrderVO venueOrderVO = new VenueOrderVO();
		model.addAttribute("venueOrderVO",venueOrderVO);
		return "front-end/venue/addVenueOrder";
	}
	
	
	
}
