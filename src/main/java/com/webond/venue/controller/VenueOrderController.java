package com.webond.venue.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.webond.employee.model.EmpService;
import com.webond.venue.dto.VenueOrderQueryDTO;
import com.webond.venue.model.VenueOrderVO;
import com.webond.venue.service.VenueOrderService;
import com.webond.venue.service.VenueService;

@Controller
@RequestMapping("/venueOrder")
public class VenueOrderController {

	@Autowired
	VenueOrderService venueOrderService;
	
	@Autowired
	VenueService venueService;
	
	VenueOrderQueryDTO venueOrderQueryDTO;
	
	@Autowired
	EmpService empService;

	@GetMapping("listAllVenueOrder")
	public String listAllVenueOrder(VenueOrderQueryDTO params ,Model model) {
		List<VenueOrderVO> list = venueOrderService.search(params);
		model.addAttribute("venueListData", list);
		model.addAttribute("empList", empService.getAll());
		model.addAttribute("params", params); // 讓表單上記住使用者打了什麼
		return "back-end/venue/listAllVenueOrder";
	}

	@GetMapping("getOneVenueOrder")
	public String getOneVenueOrder(@RequestParam("venueOrderId") Integer venueOrderId, Model model) {
		VenueOrderVO venueOrderVO = venueOrderService.getOneVenueOrder(venueOrderId);
		model.addAttribute("venueOrderVO", venueOrderVO);
		return "back-end/venue/getOneVenueOrder";
	}

}
