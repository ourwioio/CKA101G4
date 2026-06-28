package com.webond.venue.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.webond.venue.model.VenueTypeVO;
import com.webond.venue.service.VenueService;
import com.webond.venue.service.VenueTypeService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/venueType")
public class VenueTypeController {

	@Autowired
	VenueTypeService venueTypeService;

	@GetMapping("addVenueType")
	public String addVenueType(ModelMap model) {
		VenueTypeVO venueTypeVO = new VenueTypeVO();
		model.addAttribute(venueTypeVO);
		return "back-end/venueType/addVenueType";
	}

	@PostMapping("insert")
	public String insert(@Valid VenueTypeVO venueTypeVO, BindingResult result, ModelMap model) throws IOException {
		venueTypeService.addVenueType(venueTypeVO);
		return "redirect:/venueType/listAllVenueType";
	}

	@PostMapping("getOne_For_Update")
	public String getOne_For_Update(@RequestParam("venueTypeId") String venueTypeId, ModelMap model) {
		// 開始查詢資料
		VenueTypeVO venueTypeVO = venueTypeService.getOneVenueType(Integer.valueOf(venueTypeId));
		// 查詢完成，準備轉交
		model.addAttribute("venueTypeVO", venueTypeVO);
		return "back-end/venueType/listOneVenueType";
	}

	@PostMapping("update")
	public String update(@Valid VenueTypeVO venueTypeVO, BindingResult result, ModelMap model) throws IOException {
		// 開始修改資料
		venueTypeService.updateVenueType(venueTypeVO);
		// 修改完成，準備轉交
		return "redirect:/venueType/listAllVenueType";
	}

	@GetMapping("listAllVenueType")
	public String listAllVenueType(ModelMap model) {
		List<VenueTypeVO> list = venueTypeService.getAll();
		model.addAttribute("venueTypeListData", list);
		return "back-end/venueType/listAllVenueType";
	}

}
