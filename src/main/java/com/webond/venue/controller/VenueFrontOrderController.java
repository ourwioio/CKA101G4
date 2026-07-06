package com.webond.venue.controller;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.webond.member.model.MemberVO;
import com.webond.venue.dto.VenueOrderDTO;
import com.webond.venue.model.VenueOrderVO;
import com.webond.venue.model.VenueVO;
import com.webond.venue.service.VenueOrderService;
import com.webond.venue.service.VenueService;
import com.webond.venue.service.VenueSlotService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/front/venueOrder")
public class VenueFrontOrderController {

	private static final long PAYMENT_TIMEOUT_MINUTES = 5;

	@Autowired
	VenueService venueService;

	@Autowired
	VenueOrderService venueOrderService;

	@Autowired
	VenueSlotService venueSlotService;

	@GetMapping("addVenueOrder")
	public String add(@RequestParam("venueId") Integer venueId, Model model, HttpSession session) {

		MemberVO loginMember = (MemberVO) session.getAttribute("memberVO");
		if (loginMember == null) {
			return "redirect:/member/login";
		}

		VenueVO venueVO = venueService.getOneVenue(venueId);
		model.addAttribute("venueVO", venueVO);

		VenueOrderVO venueOrderVO = new VenueOrderVO();
		model.addAttribute("venueOrderVO", venueOrderVO);
		model.addAttribute("venueOrderDTO", new VenueOrderDTO());

		return "front-end/venue/addVenueOrder";
	}

	@PostMapping("insert")
	public String insert(@Valid VenueOrderDTO venueOrderDTO, 
	        BindingResult result,
	        HttpSession session,
	        Model model) {

	    MemberVO loginMember = (MemberVO) session.getAttribute("memberVO");
	    if (loginMember == null) {
	        return "redirect:/member/login";
	    }

	    if (result.hasErrors()) {
	    	VenueVO venueVO = venueService.getOneVenue(venueOrderDTO.getVenueId());
	    	model.addAttribute("venueVO", venueVO);
	    	model.addAttribute("venueOrderDTO", venueOrderDTO);
	    	return "front-end/venue/addVenueOrder";
	    	
	    }

	    VenueVO venueVO = venueService.getOneVenue(venueOrderDTO.getVenueId());

	    int hours = venueOrderDTO.getEndHour() - venueOrderDTO.getStartHour();
	    int totalAmount = hours * venueVO.getHourlyRate();

	    VenueOrderVO venueOrderVO = new VenueOrderVO();
	    venueOrderVO.setVenueVO(venueVO);
	    venueOrderVO.setMember(loginMember);
	    venueOrderVO.setVenueSlotId(venueOrderDTO.getVenueSlotId());
	    venueOrderVO.setBookDate(venueOrderDTO.getBookDate());
	    venueOrderVO.setStartAt(LocalTime.of(venueOrderDTO.getStartHour(), 0));
	    venueOrderVO.setEndAt(LocalTime.of(
	    		venueOrderDTO.getEndHour() == 24 ? 23 : venueOrderDTO.getEndHour(),
	    				venueOrderDTO.getEndHour() == 24 ? 59 : 0));
	    venueOrderVO.setTotalAmount(totalAmount);
	    venueOrderVO.setPaymentMethod(venueOrderDTO.getPaymentMethod());
	    venueOrderVO.setCreatedAt(LocalDateTime.now());
	    venueOrderVO.setOrderStatus((byte) 0);

	    venueOrderService.addVenueOrder(venueOrderVO);

	    venueSlotService.updateSlotStatus(venueOrderDTO.getVenueSlotId(), venueOrderDTO.getStartHour(), venueOrderDTO.getEndHour());

	    return "redirect:/front/venueOrder/mockPayment?venueOrderId=" + venueOrderVO.getVenueOrderId();
	}

	/** 模擬付款頁 */
	@GetMapping("mockPayment")
	public String mockPayment(@RequestParam("venueOrderId") Integer venueOrderId, HttpSession session, Model model) {

		MemberVO loginMember = (MemberVO) session.getAttribute("memberVO");
		if (loginMember == null) {
			return "redirect:/member/login";
		}

		VenueOrderVO order = venueOrderService.getOneVenueOrder(venueOrderId);
		if (order == null) {
			return "redirect:/front/venueOrder/myVenueOrder";
		}

		if (order.getOrderStatus() == 0
				&& order.getCreatedAt().plusMinutes(PAYMENT_TIMEOUT_MINUTES).isBefore(LocalDateTime.now())) {
			order.setOrderStatus((byte) 2);
			venueOrderService.updateVenueOrder(order);
			releaseSlot(order);
			return "redirect:/front/venueOrder/myVenueOrder";
		}

		if (order.getOrderStatus() != 0) {
			return "redirect:/front/venueOrder/myVenueOrder";
		}

		long secondsLeft = Duration
				.between(LocalDateTime.now(), order.getCreatedAt().plusMinutes(PAYMENT_TIMEOUT_MINUTES)).getSeconds();

		model.addAttribute("order", order);
		model.addAttribute("secondsLeft", Math.max(secondsLeft, 0));

		return "front-end/venue/mockPayment";
	}

	/** 模擬「付款成功」 */
	@PostMapping("confirmPayment")
	public String confirmPayment(@RequestParam("venueOrderId") Integer venueOrderId, HttpSession session) {

		MemberVO loginMember = (MemberVO) session.getAttribute("memberVO");
		if (loginMember == null) {
			return "redirect:/member/login";
		}

		VenueOrderVO order = venueOrderService.getOneVenueOrder(venueOrderId);
		if (order != null && order.getOrderStatus() == 0) {
			if (order.getCreatedAt().plusMinutes(PAYMENT_TIMEOUT_MINUTES).isAfter(LocalDateTime.now())) {
				order.setOrderStatus((byte) 1); // 已付款/預約成功
				order.setHandledAt(LocalDateTime.now());
				venueOrderService.updateVenueOrder(order);
				// 付款成功，時段維持已預約(1)，不用釋放
			} else {
				order.setOrderStatus((byte) 2); // 逾時取消
				venueOrderService.updateVenueOrder(order);
				releaseSlot(order);
			}
		}

		return "redirect:/front/venueOrder/myVenueOrder";
	}

	/** 倒數歸零時，前端呼叫這支把訂單標記為逾時取消 */
	@PostMapping("cancelExpired")
	public String cancelExpired(@RequestParam("venueOrderId") Integer venueOrderId, HttpSession session) {

		MemberVO loginMember = (MemberVO) session.getAttribute("memberVO");
		if (loginMember == null) {
			return "redirect:/member/login";
		}

		VenueOrderVO order = venueOrderService.getOneVenueOrder(venueOrderId);
		if (order != null && order.getOrderStatus() == 0) {
			order.setOrderStatus((byte) 2);
			venueOrderService.updateVenueOrder(order);
			releaseSlot(order);
		}

		return "redirect:/front/venueOrder/myVenueOrder";
	}

	@PostMapping("cancelPayment")
	public String cancelPayment(@RequestParam("venueOrderId") Integer venueOrderId, HttpSession session) {
		// 直接把工作丟給 cancelExpired 做，自己不重複寫邏輯
		return cancelExpired(venueOrderId, session);
	}

	@GetMapping("myVenueOrder")
	public String myVenue(HttpSession session, ModelMap model) {

		MemberVO loginMember = (MemberVO) session.getAttribute("memberVO");
		if (loginMember == null) {
			return "redirect:/member/login";
		}

		List<VenueOrderVO> list = venueOrderService.getVenuesByMember(loginMember.getMemberId());
		model.addAttribute("venueOrderListData", list);
		return "front-end/venue/myVenueOrder";
	}

	@PostMapping("updateReview")
	public String updateReview(@RequestParam("venueOrderId") Integer venueOrderId,
			@RequestParam("venueRating") Integer venueRating, @RequestParam("venueComment") String venueComment,
			HttpSession session) {

		MemberVO loginMember = (MemberVO) session.getAttribute("memberVO");
		if (loginMember == null) {
			return "redirect:/member/login";
		}

		VenueOrderVO venueOrderVO = venueOrderService.getOneVenueOrder(venueOrderId);

		if (venueOrderVO != null) {
			venueOrderVO.setVenueRating(venueRating);
			venueOrderVO.setVenueComment(venueComment);
			venueOrderService.updateVenueOrder(venueOrderVO);
		}

		return "redirect:/front/venueOrder/myVenueOrder";
	}

	/** 用訂單自己存的 startAt/endAt/venueSlotId 反推並釋放時段 */
	private void releaseSlot(VenueOrderVO order) {
		if (order.getVenueSlotId() == null)
			return;

		int startHour = order.getStartAt().getHour();
		int endHour = (order.getEndAt().getHour() == 23 && order.getEndAt().getMinute() == 59) ? 24
				: order.getEndAt().getHour();

		venueSlotService.releaseSlotStatus(order.getVenueSlotId(), startHour, endHour);
	}

}