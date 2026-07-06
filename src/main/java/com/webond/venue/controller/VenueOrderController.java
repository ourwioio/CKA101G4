package com.webond.venue.controller;

import java.util.List;

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

@Controller
@RequestMapping("/venueOrder")
public class VenueOrderController {

	@Autowired
	VenueOrderService venueOrderService;
	
	@Autowired
	VenueService venueService;

	@GetMapping("listAllVenueOrder")
	public String listAllVenueOrder(Model model) {
		List<VenueOrderVO> list = venueOrderService.getAll();
		model.addAttribute("venueListData", list);
		return "back-end/venue/listAllVenueOrder";
	}

	@GetMapping("getOneVenueOrder")
	public String getOneVenueOrder(@RequestParam("venueOrderId") Integer venueOrderId, Model model) {
		VenueOrderVO venueOrderVO = venueOrderService.getOneVenueOrder(venueOrderId);
//		VenueVO venueVO = venueService.getOneVenue(venueOrderVO.getMember().getMemberId());
		model.addAttribute("venueOrderVO", venueOrderVO);
//		model.addAttribute("venueVO",venueVO);
		return "back-end/venue/getOneVenueOrder";
	}

}
