package com.webond.venue.controller;

import java.io.IOException;
import java.time.LocalDateTime;
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
import com.webond.venue.model.VenueTypeVO;
import com.webond.venue.model.VenueVO;
import com.webond.venue.service.VenueImagesService;
import com.webond.venue.service.VenueService;
import com.webond.venue.service.VenueTypeService;

import jakarta.servlet.http.HttpSession;
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

	@GetMapping("addVenue")
	public String addVenue(ModelMap model, HttpSession session) {
		MemberVO loginMember = (MemberVO) session.getAttribute("memberVO");
		if (loginMember == null) {
			return "redirect:/member/login";
		}
		VenueVO venueVO = new VenueVO();
		model.addAttribute("venueVO", venueVO);
		return "front-end/venue/addVenue";
	}

	@PostMapping("insert")
	public String insert(@Valid VenueVO venueVO, BindingResult result, ModelMap model,
			@RequestParam("upFiles") MultipartFile[] parts,
			@RequestParam(value = "openDays", required = false) List<Integer> openDays,
			@RequestParam("startHour") int startHour, @RequestParam("endHour") int endHour,
			@RequestParam(value = "coverIndex", defaultValue = "0") int coverIndex, HttpSession session)
			throws IOException {

		MemberVO loginMember = (MemberVO) session.getAttribute("memberVO");
		if (loginMember == null) {
			return "redirect:/member/login";
		}

		/*************************** 場地類型處理 ***************************/
		Integer venueTypeId = venueVO.getVenueTypeVO() != null ? venueVO.getVenueTypeVO().getVenueTypeId() : null;
		if (venueTypeId != null) {
			VenueTypeVO venueTypeVO = venueTypeService.getOneVenueType(venueTypeId);
			venueVO.setVenueTypeVO(venueTypeVO);
		} else {
			venueVO.setVenueTypeVO(null);
		}

		model.addAttribute("savedStartHour", startHour);
		model.addAttribute("savedEndHour", endHour);
		model.addAttribute("savedOpenDays", openDays);

		if (venueVO.getAddress() != null && !venueVO.getAddress().isEmpty()) {
			model.addAttribute("savedAddress", venueVO.getAddress());
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

		venueVO.setCreatedAt(LocalDateTime.now());
		venueVO.setVenueStatus((byte) 0);
		venueVO.setRatingStars(0);
		venueVO.setRatingcount(0);

		venueVO.setMember(loginMember); // 🌟 直接用 session 裡的會員物件，不用再查一次

		venueService.addVenueWithImages(venueVO, imageBytesList, coverIndex);

		return "redirect:/front/venue/myVenue";
	}

	@PostMapping("update")
	public String update(@Valid VenueVO venueVO, BindingResult result, ModelMap model,
			@RequestParam(value = "openDays", required = false) List<Integer> openDays,
			@RequestParam("startHour") int startHour, @RequestParam("endHour") int endHour,
			@RequestParam(value = "coverImageId", required = false) Integer coverImageId, HttpSession session) {

		MemberVO loginMember = (MemberVO) session.getAttribute("memberVO");
		if (loginMember == null) {
			return "redirect:/member/login";
		}

		VenueVO existing = venueService.getOneVenue(venueVO.getVenueId());
		if (!existing.getMember().getMemberId().equals(loginMember.getMemberId())) {
			return "redirect:/front/venue/myVenue";
		}

		if (result.hasErrors()) {
			VenueVO fullVenue = venueService.getOneVenue(venueVO.getVenueId());
			fullVenue.setVenueName(venueVO.getVenueName()); // 保留使用者打的
			model.addAttribute("venueVO", fullVenue);
			return "front-end/venue/update_venue_input";
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

		venueService.updateVenueCover(venueVO, coverImageId);

		return "redirect:/front/venue/myVenue";
	}

	@PostMapping("getOne_For_Update")
	public String getOne_For_Update(@RequestParam("venueId") String venueId, ModelMap model) {
		VenueVO venueVO = venueService.getOneVenue(Integer.valueOf(venueId));
		model.addAttribute("venueVO", venueVO);
		return "front-end/venue/update_venue_input";
	}

	@GetMapping("listAllVenue")
	public String listAllVenue(ModelMap model) {
		List<VenueVO> list = venueService.getAllActive();
		model.addAttribute("venueListData", list);
		return "front-end/venue/listAllVenue";
	}

	@GetMapping("getOneVenue")
	public String getOne(@RequestParam("venueId") Integer venueId, HttpSession session, ModelMap model) {
		VenueVO venueVO = venueService.getOneVenue(venueId);
		model.addAttribute("venueVO", venueVO);
		return "front-end/venue/listOneVenue";
	}

	@GetMapping("getOneMyVenue")
	public String getOneMyvenue(@RequestParam("venueId") Integer venueId, HttpSession session, ModelMap model) {
		VenueVO venueVO = venueService.getOneVenue(venueId);
		model.addAttribute("venueVO", venueVO);

		MemberVO loginMember = (MemberVO) session.getAttribute("memberVO");
		boolean isOwner = loginMember != null && venueVO.getMember().getMemberId().equals(loginMember.getMemberId());

		if (isOwner) {
			return "front-end/venue/listOneVenue_member";
		}
		return "front-end/venue/listOneVenue";
	}

	@GetMapping("myVenue")
	public String myVenue(HttpSession session, ModelMap model) {

		MemberVO loginMember = (MemberVO) session.getAttribute("memberVO");
		if (loginMember == null) {
			return "redirect:/member/login";
		}

		List<VenueVO> list = venueService.getVenuesByMember(loginMember.getMemberId());
		model.addAttribute("venueListData", list);
		return "front-end/venue/myVenue";
	}

	@PostMapping("toggleStatus")
	public String toggleStatus(@RequestParam("venueId") Integer venueId, HttpSession session) {
		MemberVO loginMember = (MemberVO) session.getAttribute("memberVO");
		VenueVO venueVO = venueService.getOneVenue(venueId);

		// 確認是自己的場地才能操作
		if (loginMember != null && venueVO.getMember().getMemberId().equals(loginMember.getMemberId())) {
			venueService.toggleVenueStatus(venueId);
		}

		return "redirect:/front/venue/getOneMyVenue?venueId=" + venueId;
	}

	@ModelAttribute("venueTypeData")
	protected List<VenueTypeVO> venueTypeListData() {
		List<VenueTypeVO> list = venueTypeService.getAll();
		return list;
	}
}