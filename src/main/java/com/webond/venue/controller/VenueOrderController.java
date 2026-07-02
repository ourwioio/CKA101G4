package com.webond.venue.controller;

import java.time.LocalDate;
import java.time.LocalTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.webond.member.model.MemberVO;
import com.webond.member.service.MemberService;
import com.webond.venue.model.VenueOrderVO;
import com.webond.venue.model.VenueVO;
import com.webond.venue.service.VenueOrderService;
import com.webond.venue.service.VenueService;
import com.webond.venue.service.VenueSlotService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/front/venueOrder")
public class VenueOrderController {

	@Autowired
	VenueService venueService;
	
	@Autowired
	VenueOrderService venueOrderService; 
	
	@Autowired
	VenueSlotService venueSlotService;
	
	@Autowired
	MemberService memberService;
	
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
	
	@PostMapping("insert")
	public String insert(
	        @RequestParam("venueId") Integer venueId,
	        @RequestParam("venueSlotId") Integer venueSlotId,
	        @RequestParam("startHour") int startHour,
	        @RequestParam("endHour") int endHour,
	        @RequestParam("paymentMethod") Byte paymentMethod,
	        HttpSession session) {

	    // 1. 驗證登入
	    Integer memberId = (Integer) session.getAttribute("loginMemberId");
	    if (memberId == null) {
	        return "redirect:/front/venue/fakeLogin";
	    }

	    // 2. 撈場地和會員資料
	    VenueVO venueVO = venueService.getOneVenue(venueId);
	    MemberVO member = memberService.getOneMember(memberId);

	    // 3. 計算費用
	    int hours = endHour - startHour;
	    int totalAmount = hours * venueVO.getHourlyRate();

	    // 4. 建立訂單
	    VenueOrderVO venueOrderVO = new VenueOrderVO();
	    venueOrderVO.setVenueVO(venueVO);
	    venueOrderVO.setMember(member);
	    venueOrderVO.setStartAt(LocalTime.of(startHour, 0));
	    venueOrderVO.setEndAt(LocalTime.of(endHour == 24 ? 23 : endHour, endHour == 24 ? 59 : 0));
	    venueOrderVO.setTotalAmount(totalAmount);
	    venueOrderVO.setPaymentMethod(paymentMethod);
	    venueOrderVO.setCreatedAt(LocalDate.now());
	    venueOrderVO.setRefundStatus((byte) 0);

	    venueOrderService.addVenueOrder(venueOrderVO);

	    // 5. 更新場地時段狀態
	    venueSlotService.updateSlotStatus(venueSlotId, startHour, endHour);

	    return "redirect:/front/venue/listAllVenue";
	}
	
	
	
	
	
}
