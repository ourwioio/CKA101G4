package com.webond.venue.controller;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.webond.member.model.MemberVO;
import com.webond.member.model.NotificationVO;
import com.webond.member.service.NotificationService;
import com.webond.venue.dto.VenueOrderFrontDTO;
import com.webond.venue.model.VenueOrderVO;
import com.webond.venue.model.VenueTypeVO;
import com.webond.venue.model.VenueVO;
import com.webond.venue.service.VenueOrderService;
import com.webond.venue.service.VenueReportService;
import com.webond.venue.service.VenueService;
import com.webond.venue.service.VenueSlotService;
import com.webond.venue.service.VenueTypeService;

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
	
	@Autowired
	NotificationService notificationService;

	@Autowired
	VenueTypeService venueTypeService;

	@Autowired
	VenueReportService venueReportService;

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
		model.addAttribute("venueOrderDTO", new VenueOrderFrontDTO());

		return "front-end/venue/addVenueOrder";
	}

	@PostMapping("insert")
	public String insert(@ModelAttribute("venueOrderDTO") @Valid VenueOrderFrontDTO venueOrderDTO, BindingResult result,
			HttpSession session, Model model) {
		
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

		if (venueVO != null && venueVO.getVenueSlots() != null) {
			venueVO.getVenueSlots().size(); // 呼叫 .size() 就能成功觸發 Lazy Loading
		}

		int hours = venueOrderDTO.getEndHour() - venueOrderDTO.getStartHour();
		int totalAmount = hours * venueVO.getHourlyRate();

		VenueOrderVO venueOrderVO = new VenueOrderVO();
		venueOrderVO.setVenueVO(venueVO);
		venueOrderVO.setMember(loginMember);
		venueOrderVO.setVenueSlotId(venueOrderDTO.getVenueSlotId());
		venueOrderVO.setBookDate(venueOrderDTO.getBookDate());
		venueOrderVO.setStartAt(LocalTime.of(venueOrderDTO.getStartHour(), 0));
		venueOrderVO.setEndAt(LocalTime.of(venueOrderDTO.getEndHour() == 24 ? 23 : venueOrderDTO.getEndHour(),
				venueOrderDTO.getEndHour() == 24 ? 59 : 0));
		venueOrderVO.setTotalAmount(totalAmount);
		venueOrderVO.setPaymentMethod(venueOrderDTO.getPaymentMethod());
		venueOrderVO.setCreatedAt(LocalDateTime.now());
		venueOrderVO.setOrderStatus((byte) 0);
		venueOrderVO.setPayoutAmount((byte) 0);

		try {
			// 呼叫 Service，這步會啟動悲觀鎖並把字串改成 '3'
			venueOrderService.addVenueOrder(venueOrderVO);
			
		} catch (RuntimeException e) {
			// 💡 關鍵：如果被別人搶先一步佔用，Service會噴錯，這裡會抓住它
			// 把錯誤訊息掛在 startHour 欄位上，讓前端網頁顯示紅字
			result.rejectValue("startHour", "error.venueOrderDTO", e.getMessage());

			// 重新準備資料，讓使用者留在原頁面看錯誤訊息
			model.addAttribute("venueVO", venueVO);
			model.addAttribute("venueOrderDTO", venueOrderDTO);
			return "front-end/venue/addVenueOrder";
		}
		
		

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

		// 已經不是「待付款」狀態（已付款或已取消），不用再顯示付款頁
		if (order.getOrderStatus() != 0) {
			return "redirect:/front/venueOrder/myVenueOrder";
		}

		// 真的超過付款時限了，才標記逾時取消、釋放時段
		if (order.getCreatedAt().plusMinutes(PAYMENT_TIMEOUT_MINUTES).isBefore(LocalDateTime.now())) {
			order.setOrderStatus((byte) 2);
			order.setHandledAt(LocalDateTime.now());
			venueOrderService.updateVenueOrder(order);
			releaseSlot(order);
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
				confirmSlot(order); // ⚙️ 新增：把時段字串從 '3' 正式扶正為 '1'
				
				VenueVO venueVO = venueService.getOneVenue(order.getVenueVO().getVenueId());
				// 新增通知給場地主
				NotificationVO notificationVO = new NotificationVO();
				notificationVO.setMember(venueVO.getMember());
				notificationVO.setTitle("場地新預約通知");
				notificationVO.setContent(venueVO.getVenueName() + "被預約的時段是" + order.getStartAt() + " ~ " + order.getEndAt());
				notificationVO.setNotificationType((byte) 0);
				notificationService.addNotification(notificationVO);
				
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

		List<Integer> orderIds = list.stream().map(VenueOrderVO::getVenueOrderId).collect(Collectors.toList());
		model.addAttribute("reportedOrderIds", venueReportService.getReportedOrderIds(orderIds));

		LocalDate cutoff = LocalDate.now().plusDays(3);
	    model.addAttribute("cancelCutoff", cutoff);

		return "front-end/venue/myVenueOrder";
	}

	@PostMapping("updateReview")
	public String updateReview(@RequestParam("venueOrderId") Integer venueOrderId,
	        @RequestParam("venueRating") Integer venueRating, 
	        @RequestParam("venueComment") String venueComment,
	        HttpSession session) {

	    MemberVO loginMember = (MemberVO) session.getAttribute("memberVO");
	    if (loginMember == null) {
	        return "redirect:/member/login";
	    }

	    try {
	        venueOrderService.submitReview(venueOrderId, loginMember.getMemberId(), venueRating, venueComment);
	        
	        VenueOrderVO venueOrderVO = venueOrderService.getOneVenueOrder(venueOrderId);
	        VenueVO venueVO = venueService.getOneVenue(venueOrderVO.getVenueVO().getVenueId());
	        // 新增通知給場地主
			NotificationVO notificationVO = new NotificationVO();
			notificationVO.setMember(venueVO.getMember());
			notificationVO.setTitle("場地被評價通知");
			notificationVO.setContent("您的場地" + venueVO.getVenueName() + "已被評價");
			notificationVO.setNotificationType((byte) 2);
			notificationService.addNotification(notificationVO);
	        
	    } catch (RuntimeException e) {
	        // 之後可以視需要用 RedirectAttributes 帶錯誤訊息回頁面顯示
	        // 目前先讓它靜默失敗、不讓整個 request 500
	    }

	    return "redirect:/front/venueOrder/myVenueOrder";
	}

	@GetMapping("myVenuesReservations")
	public String getBookingsForMyVenues(Model model, HttpSession session) {
		MemberVO loginMember = (MemberVO) session.getAttribute("memberVO");
		if (loginMember == null) {
			return "redirect:/member/login";
		}
		List<VenueOrderVO> list = venueOrderService.getMyAllReservations(loginMember.getMemberId());
		model.addAttribute("myReservationslist", list);
		return "front-end/venue/myVenuesReservations";
	}
	
	@GetMapping("getOneReservations")
	public String getOneReservations(@RequestParam("venueOrderId") Integer venueOrderId, Model model) {
		VenueOrderVO venueOrderVO = venueOrderService.getOneVenueOrder(venueOrderId);
		model.addAttribute("venueOrderVO", venueOrderVO);
		return "front-end/venue/getOneReservations";
	}
	
	@GetMapping("myVenuesCompleted")
	public String getMyVenuesCompleted(Model model, HttpSession session) {
		MemberVO loginMember = (MemberVO) session.getAttribute("memberVO");
		if (loginMember == null) {
			return "redirect:/member/login";
		}
		List<VenueOrderVO> list = venueOrderService.getMyAllCompletedBookings(loginMember.getMemberId());
		model.addAttribute("myAllCompletedlist", list);
		return "front-end/venue/myVenuesCompleted";
	}
	
	@PostMapping("cancelOrder")
	public String cancelOrder(@RequestParam("venueOrderId") Integer venueOrderId,
	        @RequestParam("refundReason") String refundReason,
	        HttpSession session) {

	    MemberVO loginMember = (MemberVO) session.getAttribute("memberVO");
	    if (loginMember == null) {
	        return "redirect:/member/login";
	    }

	    VenueOrderVO venueOrderVO = venueOrderService.getOneVenueOrder(venueOrderId);
	    if (venueOrderVO != null) {
	        venueOrderVO.setOrderStatus((byte) 4);
	        venueOrderVO.setRefundStatus((byte) 0);
	        venueOrderVO.setRefundReason(refundReason);
	        venueOrderService.updateVenueOrder(venueOrderVO);
	    }
	    VenueVO venueVO = venueService.getOneVenue(venueOrderVO.getVenueVO().getVenueId());
		// 新增通知給場地主
		NotificationVO notificationVO = new NotificationVO();
		notificationVO.setMember(venueVO.getMember());
		notificationVO.setTitle("場地訂單取消通知");
		notificationVO.setContent("您的" + venueVO.getVenueName() + "預約已被取消");
		notificationVO.setNotificationType((byte) 0);
		notificationService.addNotification(notificationVO);
	    
	    return "redirect:/front/venueOrder/myVenueOrder";
	}
	
	@ModelAttribute("venueTypeData")
	protected List<VenueTypeVO> listData(){
		List<VenueTypeVO> list = venueTypeService.getAll();
		return list;
	}

	/** 用訂單自己存的 startAt/endAt/venueSlotId 反推並釋放時段 */
	private void releaseSlot(VenueOrderVO order) {
		if (order.getVenueSlotId() == null)
			return;

		int startHour = order.getStartAt().getHour();
		int endHour = (order.getEndAt().getHour() == 23 && order.getEndAt().getMinute() == 59) ? 24
				: order.getEndAt().getHour();

		// ⚙️ 改為呼叫帶有防呆機制、只針對 '3' 進行還原的方法
		venueSlotService.releaseTimeoutSlot(order.getVenueSlotId(), startHour, endHour);
	}

	/** 用訂單自己存的 startAt/endAt/venueSlotId 反推並確認時段（'3' 改為為 '1'） */
	private void confirmSlot(VenueOrderVO order) {
		if (order.getVenueSlotId() == null)
			return;

		int startHour = order.getStartAt().getHour();
		int endHour = (order.getEndAt().getHour() == 23 && order.getEndAt().getMinute() == 59) ? 24
				: order.getEndAt().getHour();

		venueSlotService.confirmSlotPayment(order.getVenueSlotId(), startHour, endHour);
	}

}