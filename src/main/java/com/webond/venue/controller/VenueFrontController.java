package com.webond.venue.controller;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.webond.member.model.MemberVO;
import com.webond.member.service.MemberService;
import com.webond.venue.model.VenueTypeVO;
import com.webond.venue.model.VenueVO;
import com.webond.venue.service.VenueImagesService;
import com.webond.venue.service.VenueService;
import com.webond.venue.service.VenueTypeService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/front/venue")
public class VenueFrontController {

	@Autowired
	VenueService venueService;

	@Autowired
	VenueImagesService venueImagesService;

	@Autowired
	VenueTypeService venueTypeService;

	@Autowired
	MemberService memberService;

	@GetMapping("addVenue")
	public String addVenue(ModelMap model) {
		VenueVO venueVO = new VenueVO();
		model.addAttribute("venueVO", venueVO);
		return "front-end/venue/addVenue";
	}

	@PostMapping("insert")
	public String insert(@Valid VenueVO venueVO, BindingResult result, ModelMap model,
			@RequestParam("upFiles") MultipartFile[] parts,
			@RequestParam(value = "openDays", required = false) List<Integer> openDays,
			@RequestParam("startHour") int startHour, @RequestParam("endHour") int endHour) throws IOException {

		Integer venueTypeId = venueVO.getVenueTypeVO() != null ? venueVO.getVenueTypeVO().getVenueTypeId() : null;

		if (venueTypeId != null) {
			VenueTypeVO venueTypeVO = venueTypeService.getOneVenueType(venueTypeId);
			venueVO.setVenueTypeVO(venueTypeVO);
		} else {
			venueVO.setVenueTypeVO(null);
		}

		if (venueVO.getVenueTypeVO() == null) {
			model.addAttribute("venueTypeError", "場地類型: 請選擇場地類型");
			return "front-end/venue/addVenue";
		}

		if (parts == null || parts.length == 0 || parts[0].isEmpty()) {
			model.addAttribute("errorMessage", "場地照片: 請至少上傳一張照片");
			model.addAttribute("venueVO", venueVO);
			return "front-end/venue/addVenue";
		}

		if (result.hasErrors()) {
			String address = venueVO.getAddress();
			if (address != null && !address.isEmpty()) {
				model.addAttribute("savedAddress", address);
			}
			return "front-end/venue/addVenue";
		}

		List<byte[]> imageBytesList = new ArrayList<>();
		for (MultipartFile multipartFile : parts) {
			if (!multipartFile.isEmpty()) {
				imageBytesList.add(multipartFile.getBytes());
			}
		}

		StringBuilder daysSb = new StringBuilder("0000000");
		if (openDays != null) {
			for (Integer day : openDays) {
				if (day >= 0 && day <= 6)
					daysSb.setCharAt(day, '1');
			}
		}
		venueVO.setOpenDays(daysSb.toString());

		StringBuilder hoursSb = new StringBuilder("222222222222222222222222");
		for (int h = startHour; h < endHour; h++) {
			if (h >= 0 && h < 24)
				hoursSb.setCharAt(h, '0');
		}
		venueVO.setAvailableHours(hoursSb.toString());

		venueVO.setCreatedAt(LocalDate.now());
		venueVO.setVenueStatus((byte) 0);
		venueVO.setRatingStars(0);
		venueVO.setRatingcount(0);

// **********先暫時用假的會員ID，等登入功能做好再換從session取得ID**********
		MemberVO member = memberService.getOneMember(8);
		venueVO.setMember(member);

		venueService.addVenueWithImages(venueVO, imageBytesList);

		return "redirect:/front/venue/listAllVenue";
	}

	@PostMapping("getOne_For_Update")
	public String getOne_For_Update(@RequestParam("venueId") String venueId, ModelMap model) {
		/*************************** 1.接收請求參數 - 輸入格式的錯誤處理 ************************/
		/*************************** 2.開始查詢資料 *****************************************/
		VenueVO venueVO = venueService.getOneVenue(Integer.valueOf(venueId));

		/*************************** 3.查詢完成,準備轉交(Send the Success view) **************/
		model.addAttribute("venueVO", venueVO);
		return "back-end/venue/update_venue_input";
	}
	
	@GetMapping("listAllVenue")
	public String listAllVenue(ModelMap model) {
		List<VenueVO> list = venueService.getAllActive();
		model.addAttribute("venueListData", list);
		return "front-end/venue/listAllVenue";
	}

	@GetMapping("getOneVenue")
	public String getOne(@RequestParam("venueId") Integer venueId, ModelMap model) {
		VenueVO venueVO = venueService.getOneVenue(venueId);
		model.addAttribute("venueVO", venueVO);
		return "front-end/venue/listOneVenue";
	}

	@ModelAttribute("venueTypeData")
	protected List<VenueTypeVO> venueTypeListData() {
		List<VenueTypeVO> list = venueTypeService.getAll();
		return list;
	}
}
