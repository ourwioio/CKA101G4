package com.webond.activity.controller;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.webond.activity.model.ActivityOrderService;
import com.webond.activity.model.ActivityOrderVO;
import com.webond.activity.model.ActivityNotificationService;
import com.webond.activity.model.ActivityService;
import com.webond.activity.model.ActivityTypeService;
import com.webond.activity.model.ActivityVO;
import com.webond.member.model.MemberVO;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
public class FrontActivityController {

	private static final String ACTIVITY_LOGIN_MEMBER_ID = "activityLoginMemberId";
	private static final List<Integer> TEST_MEMBER_IDS = Arrays.asList(1, 2, 3);
	private static final String DEFAULT_ACTIVITY_IMAGE_PATH = "static/images/activity/default-activity.jpg";
	private static final String ACTIVITY_IMAGE_STATIC_DIR = "static/images/activity/";

	@Autowired
	private ActivityService activitySvc;

	@Autowired
	private ActivityOrderService activityOrderSvc;

	@Autowired
	private ActivityTypeService activityTypeSvc;

	@Autowired
	private ActivityNotificationService activityNotificationSvc;

	@GetMapping("/activity/front/home")
	public String frontActHome(Model model, HttpSession session) {
		addFakeLoginMember(model, session);
		return "front-end/activity/frontActHome";
	}

	@PostMapping("/activity/front/fakeLogin")
	public String switchFakeLogin(@RequestParam(value = "memberId", required = false) Integer memberId,
			HttpSession session) {
		if (memberId != null && TEST_MEMBER_IDS.contains(memberId)) {
			session.setAttribute(ACTIVITY_LOGIN_MEMBER_ID, memberId);
		} else {
			session.removeAttribute(ACTIVITY_LOGIN_MEMBER_ID);
		}
		return "redirect:/activity/front/home";
	}

	@PostMapping("/activity/front/syncAttendees")
	public String syncAttendeesFromOrders(HttpSession session) {
		if (!isLoginMember(session)) {
			return "redirect:/activity/front/home?loginRequired=true";
		}

		activitySvc.syncAttendeesFromOrders();
		return "redirect:/activity/front/home?syncSuccess=true";
	}

	@GetMapping("/activity/front/list")
	public String frontActivityList(@RequestParam(value = "keyword", required = false) String keyword,
			@RequestParam(value = "typeId", required = false) Integer typeId,
			@RequestParam(value = "status", required = false) Byte status,
			@RequestParam(value = "onlyAvailable", required = false) Boolean onlyAvailable,
			@RequestParam(value = "sort", required = false) String sort, Model model, HttpSession session) {

		addFakeLoginMember(model, session);
		Integer loginMemberId = getLoginMemberId(session);
		List<ActivityVO> activityList = activitySvc.searchActivities(keyword, typeId, status, onlyAvailable, sort,
				loginMemberId);
		model.addAttribute("activityListData",
				activityList);
		model.addAttribute("registrationStatusMap", buildRegistrationStatusMap(activityList));
		model.addAttribute("typeListData", activityTypeSvc.getAll());
		model.addAttribute("keyword", keyword);
		model.addAttribute("selectedTypeId", typeId);
		model.addAttribute("selectedStatus", status);
		model.addAttribute("onlyAvailable", Boolean.TRUE.equals(onlyAvailable));
		model.addAttribute("sort", sort);

		return "front-end/activity/frontActivityList";
	}

	@GetMapping("/activity/front/detail")
	public String frontActivityDetail(@RequestParam("id") Integer activityId,
			@RequestParam(value = "from", required = false) String from,
			@RequestParam(value = "full", required = false) Boolean full,
			@RequestParam(value = "duplicate", required = false) Boolean duplicate,
			@RequestParam(value = "owner", required = false) Boolean owner,
			@RequestParam(value = "closed", required = false) Boolean closed, Model model, HttpSession session) {

		addFakeLoginMember(model, session);

		ActivityVO activityVO = activitySvc.getOneActivity(activityId);
		boolean activityEnded = activitySvc.isActivityEnded(activityId);
		
		
		
		List<ActivityOrderVO> orderListData = activityOrderSvc.getOrdersByActivityId(activityId);
		double totalScore = 0;
	    int ratedOrderCount = 0; 

	    if (orderListData != null && !orderListData.isEmpty()) {
	        for (ActivityOrderVO order : orderListData) {
	            if (order.getBuyerRateSeller() != null && order.getBuyerRateSeller() > 0) {
	                totalScore += order.getBuyerRateSeller();
	                ratedOrderCount++;
	            }
	        }
	    }
	    double averageScore = (ratedOrderCount > 0) ? (totalScore / ratedOrderCount) : 5.0;
	    model.addAttribute("averageScore", averageScore);
		
	    
	    
		model.addAttribute("activityVO", activityVO);
		model.addAttribute("registrationStatusText", getRegistrationStatusText(activityVO));
		model.addAttribute("backUrl", resolveDetailBackUrl(from));
		model.addAttribute("backText", resolveDetailBackText(from));
		model.addAttribute("showOrderButton", isLoginMember(session) && (from == null || from.trim().isEmpty())
				&& !activityEnded && activitySvc.isRegistrationOpen(activityId));
		model.addAttribute("showLoginPrompt", !isLoginMember(session) && (from == null || from.trim().isEmpty())
				&& !activityEnded);
		model.addAttribute("showReviewButton", activityEnded);
		model.addAttribute("reviewListData", activityOrderSvc.getReviewedOrdersByActivityId(activityId));

		if (Boolean.TRUE.equals(full)) {
			model.addAttribute("fullMessage", "活動已額滿，無法報名。");
		}

		if (Boolean.TRUE.equals(duplicate)) {
			model.addAttribute("duplicateMessage", "你已經報名過這個活動，不能重複報名。");
		}

		if (Boolean.TRUE.equals(owner)) {
			model.addAttribute("ownerMessage", "主辦者不能報名自己舉辦的活動。");
		}

		if (Boolean.TRUE.equals(closed)) {
			model.addAttribute("closedMessage", "目前不在報名開放時間內，無法報名。");
		}

		return "front-end/activity/frontActivityDetail";
	}

	private String resolveDetailBackUrl(String from) {
		if ("host".equals(from)) {
			return "/activity/front/myHostActivity";
		}
		if ("admin".equals(from)) {
			return "/activity/listAllActivity";
		}
		return "/activity/front/list";
	}

	private String resolveDetailBackText(String from) {
		if ("host".equals(from)) {
			return "返回我舉辦的活動";
		}
		if ("admin".equals(from)) {
			return "返回活動管理";
		}
		return "返回列表";
	}

	@GetMapping("/activity/front/image")
	public ResponseEntity<byte[]> activityImage(@RequestParam(value = "id", required = false) Integer activityId)
			throws IOException {
		ActivityVO activityVO = null;
		if (activityId != null) {
			activityVO = activitySvc.getOneActivity(activityId);
			if (activityVO != null && activityVO.getActivityImage() != null
					&& activityVO.getActivityImage().length > 0) {
				String imageType = activityVO.getActivityImageType() == null ? "image/jpeg"
						: activityVO.getActivityImageType();
				return ResponseEntity.ok()
						.contentType(MediaType.parseMediaType(imageType))
						.body(activityVO.getActivityImage());
			}
		}

		ClassPathResource defaultImage = resolveDefaultActivityImage(activityVO);
		return ResponseEntity.ok()
				.contentType(resolveActivityImageMediaType(defaultImage.getFilename()))
				.body(defaultImage.getInputStream().readAllBytes());
	}

	private ClassPathResource resolveDefaultActivityImage(ActivityVO activityVO) {
		if (activityVO != null && activityVO.getActivityTypeId() != null) {
			ClassPathResource jpgImage = new ClassPathResource(
					ACTIVITY_IMAGE_STATIC_DIR + activityVO.getActivityTypeId() + ".jpg");
			if (jpgImage.exists()) {
				return jpgImage;
			}

			ClassPathResource pngImage = new ClassPathResource(
					ACTIVITY_IMAGE_STATIC_DIR + activityVO.getActivityTypeId() + ".png");
			if (pngImage.exists()) {
				return pngImage;
			}
		}

		return new ClassPathResource(DEFAULT_ACTIVITY_IMAGE_PATH);
	}

	private MediaType resolveActivityImageMediaType(String filename) {
		if (filename != null && filename.toLowerCase().endsWith(".png")) {
			return MediaType.IMAGE_PNG;
		}
		return MediaType.IMAGE_JPEG;
	}

	@GetMapping("/activity/front/order")
	public String frontActivityOrder(@RequestParam("activityId") Integer activityId, Model model, HttpSession session) {
		Integer loginMemberId = getLoginMemberId(session);
		if (loginMemberId == null) {
			return "redirect:/activity/front/home?loginRequired=true";
		}
		addFakeLoginMember(model, session);

		ActivityVO activityVO = activitySvc.getOneActivity(activityId);

		if (activityVO == null) {
			return "redirect:/activity/front/list";
		}

		if (activitySvc.isActivityEnded(activityId)) {
			return "redirect:/activity/front/detail?id=" + activityId + "&closed=true";
		}

		if (loginMemberId.equals(activityVO.getMemberId())) {
			return "redirect:/activity/front/detail?id=" + activityId + "&owner=true";
		}

		if (!activitySvc.isRegistrationOpen(activityId)) {
			return "redirect:/activity/front/detail?id=" + activityId + "&closed=true";
		}

		ActivityOrderVO orderVO = new ActivityOrderVO();
		orderVO.setActivityId(activityVO.getActivityId());
		orderVO.setBuyerMemberId(loginMemberId);
		orderVO.setBookingCount(1);
		orderVO.setActivityPrice(activityVO.getActivityPrice());
		orderVO.setTotalAmount(activityVO.getActivityPrice());
		orderVO.setOrderStatus((byte) 2);
		orderVO.setActivityPaymentMethod((byte) 0);

		model.addAttribute("activityVO", activityVO);
		model.addAttribute("activityOrderVO", orderVO);

		return "front-end/activity/frontActivityOrder";
	}

	@PostMapping("/activity/front/order/save")
	public String saveFrontOrder(@ModelAttribute("activityOrderVO") ActivityOrderVO orderVO, HttpSession session) {
		Integer bookingCount = orderVO.getBookingCount();
		Integer activityPrice = orderVO.getActivityPrice();
		Integer loginMemberId = getLoginMemberId(session);
		if (loginMemberId == null) {
			return "redirect:/activity/front/home?loginRequired=true";
		}
		ActivityVO activityVO = activitySvc.getOneActivity(orderVO.getActivityId());

		if (bookingCount == null || bookingCount < 1) {
			bookingCount = 1;
		}

		if (activityVO == null) {
			return "redirect:/activity/front/list";
		}

		if (activitySvc.isActivityEnded(orderVO.getActivityId())) {
			return "redirect:/activity/front/detail?id=" + orderVO.getActivityId() + "&closed=true";
		}

		if (loginMemberId.equals(activityVO.getMemberId())) {
			return "redirect:/activity/front/detail?id=" + orderVO.getActivityId() + "&owner=true";
		}

		if (!activitySvc.isRegistrationOpen(orderVO.getActivityId())) {
			return "redirect:/activity/front/detail?id=" + orderVO.getActivityId() + "&closed=true";
		}

		orderVO.setBuyerMemberId(loginMemberId);
		orderVO.setBookingCount(bookingCount);
		orderVO.setTotalAmount(activityPrice * bookingCount);
		orderVO.setOrderStatus((byte) 2);

		if (activityOrderSvc.hasActiveOrder(orderVO.getActivityId(), loginMemberId)) {
			return "redirect:/activity/front/detail?id=" + orderVO.getActivityId() + "&duplicate=true";
		}

		if (!activitySvc.canRegister(orderVO.getActivityId(), orderVO.getBookingCount())) {
			return "redirect:/activity/front/detail?id=" + orderVO.getActivityId() + "&full=true";
		}

		ActivityOrderVO savedOrder = activityOrderSvc.addOrder(orderVO);
		activityNotificationSvc.notifyHostNewOrder(activityVO, savedOrder);

		return "redirect:/activity/front/myOrder";
	}

	@PostMapping("/activity/front/order/cancel")
	public String cancelOrder(@RequestParam("activityOrderId") Integer activityOrderId, HttpSession session) {
		if (!isLoginMember(session)) {
			return "redirect:/activity/front/home?loginRequired=true";
		}

		ActivityOrderVO existingOrder = activityOrderSvc.getOneOrder(activityOrderId);

		if (existingOrder != null && activitySvc.isActivityEnded(existingOrder.getActivityId())) {
			return "redirect:/activity/front/myOrder?cannotCancelEnded=true";
		}

		ActivityOrderVO orderVO = activityOrderSvc.cancelOrder(activityOrderId, getLoginMemberId(session));

		if (orderVO != null) {
			activitySvc.syncAttendeesFromOrders(orderVO.getActivityId());
			activityNotificationSvc.notifyHostOrderCancelled(activitySvc.getOneActivity(orderVO.getActivityId()),
					orderVO, existingOrder == null ? null : existingOrder.getOrderStatus());
		}

		return "redirect:/activity/front/myOrder";
	}

	@PostMapping("/activity/front/order/refund")
	public String requestRefund(@RequestParam("activityOrderId") Integer activityOrderId,
			@RequestParam(value = "refundReason", required = false) String refundReason, HttpSession session) {
		if (!isLoginMember(session)) {
			return "redirect:/activity/front/home?loginRequired=true";
		}

		ActivityOrderVO orderVO = activityOrderSvc.requestRefund(activityOrderId, getLoginMemberId(session), refundReason);

		if (orderVO != null) {
			activitySvc.syncAttendeesFromOrders(orderVO.getActivityId());
		}

		return "redirect:/activity/front/myOrder";
	}

	@PostMapping("/activity/front/order/pay")
	public String payOrder(@RequestParam("activityOrderId") Integer activityOrderId,
			@RequestParam("activityPaymentMethod") Byte activityPaymentMethod, HttpSession session) {
		if (!isLoginMember(session)) {
			return "redirect:/activity/front/home?loginRequired=true";
		}

		ActivityOrderVO orderVO = activityOrderSvc.getOneOrder(activityOrderId);

		if (orderVO == null) {
			return "redirect:/activity/front/myOrder";
		}

		boolean hasCapacity = activitySvc.canRegister(orderVO.getActivityId(), orderVO.getBookingCount());
		ActivityOrderVO paidOrder = activityOrderSvc.payOrder(activityOrderId, getLoginMemberId(session),
				activityPaymentMethod, hasCapacity);

		if (paidOrder != null) {
			activitySvc.syncAttendeesFromOrders(paidOrder.getActivityId());
			ActivityVO activityVO = activitySvc.getOneActivity(paidOrder.getActivityId());
			if (activitySvc.isFull(activityVO)) {
				activityOrderSvc.failPendingPaymentOrdersByFullActivity(paidOrder.getActivityId());
			}
			if (paidOrder.getOrderStatus() != null && paidOrder.getOrderStatus() == 1) {
				return "redirect:/activity/front/myOrder?paymentCancelled=true";
			}
			if (paidOrder.getOrderStatus() != null && paidOrder.getOrderStatus() == 0) {
				activityNotificationSvc.notifyHostPaymentCompleted(activityVO, paidOrder);
			}
		}

		return "redirect:/activity/front/myOrder?paySuccess=true";
	}

	@GetMapping("/activity/front/myOrder")
	public String myOrder(Model model, HttpSession session) {
		Integer loginMemberId = getLoginMemberId(session);
		if (loginMemberId == null) {
			return "redirect:/activity/front/home?loginRequired=true";
		}

		addFakeLoginMember(model, session);
		List<ActivityOrderVO> orderList = activityOrderSvc.getOrdersByBuyerMemberId(loginMemberId);
		model.addAttribute("orderListData", orderList);
		model.addAttribute("activityListData", activitySvc.getAll());
		model.addAttribute("shouldAutoRefreshOrder", hasOrderStatus(orderList, (byte) 2)
				|| hasOrderStatus(orderList, (byte) 3));

		return "front-end/activity/myActivityOrder";
	}

	@GetMapping("/activity/front/order/status")
	@ResponseBody
	public Map<Integer, Map<String, Object>> activityOrderStatus(
			@RequestParam("orderIds") String orderIds, HttpSession session) {
		Map<Integer, Map<String, Object>> statusMap = new HashMap<>();
		Integer loginMemberId = getLoginMemberId(session);

		if (loginMemberId == null || orderIds == null || orderIds.trim().isEmpty()) {
			return statusMap;
		}

		activityOrderSvc.completeEndedPaidOrders();

		for (String rawOrderId : orderIds.split(",")) {
			Integer orderId = parseInteger(rawOrderId);
			if (orderId == null) {
				continue;
			}
			ActivityOrderVO orderVO = activityOrderSvc.getOneOrder(orderId);
			if (orderVO == null || !canViewActivityOrderStatus(orderVO, loginMemberId)) {
				continue;
			}

			Map<String, Object> status = new HashMap<>();
			status.put("orderStatus", orderVO.getOrderStatus());
			status.put("refundStatus", orderVO.getRefundStatus());
			statusMap.put(orderId, status);
		}

		return statusMap;
	}

	@GetMapping("/activity/front/addHostActivity")
	public String addHostActivity(Model model, HttpSession session) {
		Integer loginMemberId = getLoginMemberId(session);
		if (loginMemberId == null) {
			return "redirect:/activity/front/home?loginRequired=true";
		}
		ActivityVO activityVO = new ActivityVO();

		activityVO.setMemberId(loginMemberId);
		activityVO.setAttendeesCount(0);
		activityVO.setActivityStatus((byte) 0);

		addFakeLoginMember(model, session);
		model.addAttribute("activityVO", activityVO);
		model.addAttribute("typeListData", activityTypeSvc.getAll());
		model.addAttribute("formTitle", "我要辦活動");
		model.addAttribute("submitText", "建立活動");
		model.addAttribute("cancelUrl", "/activity/front/myHostActivity");
		model.addAttribute("cancelText", "返回我舉辦的活動");
		model.addAttribute("previewImageUrl", "/images/activity/default-activity.jpg");

		return "front-end/activity/frontAddHostActivity";
	}

	@GetMapping("/activity/front/repeatHostActivity")
	public String repeatHostActivity(@RequestParam("id") Integer sourceActivityId, Model model, HttpSession session) {
		Integer loginMemberId = getLoginMemberId(session);
		if (loginMemberId == null) {
			return "redirect:/activity/front/home?loginRequired=true";
		}
		ActivityVO sourceVO = activitySvc.getOneActivity(sourceActivityId);

		if (sourceVO == null) {
			return "redirect:/activity/front/myHostActivity";
		}

		if (!sourceVO.getMemberId().equals(loginMemberId)) {
			return "redirect:/activity/front/myHostActivity?noPermission=true";
		}

		ActivityVO activityVO = new ActivityVO();
		activityVO.setActivityTypeId(sourceVO.getActivityTypeId());
		activityVO.setMemberId(loginMemberId);
		activityVO.setActivityTitle(sourceVO.getActivityTitle());
		activityVO.setActivityDescription(sourceVO.getActivityDescription());
		activityVO.setActivityPrice(sourceVO.getActivityPrice());
		activityVO.setMinParticipants(sourceVO.getMinParticipants());
		activityVO.setMaxParticipants(sourceVO.getMaxParticipants());
		activityVO.setAttendeesCount(0);
		activityVO.setActivityStatus((byte) 0);

		addFakeLoginMember(model, session);
		model.addAttribute("activityVO", activityVO);
		model.addAttribute("typeListData", activityTypeSvc.getAll());
		model.addAttribute("sourceActivityId", sourceActivityId);
		model.addAttribute("formTitle", "再次舉辦活動");
		model.addAttribute("submitText", "建立新場次");
		model.addAttribute("cancelUrl", "/activity/front/myHostActivity");
		model.addAttribute("cancelText", "返回我舉辦的活動");
		model.addAttribute("previewImageUrl", "/activity/front/image?id=" + sourceActivityId);

		return "front-end/activity/frontAddHostActivity";
	}

	@PostMapping("/activity/front/insertHostActivity")
	public String insertHostActivity(@Valid @ModelAttribute("activityVO") ActivityVO activityVO, BindingResult result,
			@RequestParam(value = "sourceActivityId", required = false) Integer sourceActivityId,
			@RequestParam(value = "activityImageFile", required = false) MultipartFile activityImageFile, Model model,
			HttpSession session) throws IOException {
		if (!isLoginMember(session)) {
			return "redirect:/activity/front/home?loginRequired=true";
		}

		activityVO.setMemberId(getLoginMemberId(session));
		String imageError = applyActivityImage(activityVO, activityImageFile);
		applyRepeatedActivityImage(activityVO, sourceActivityId, activityImageFile, session);

		if (activityVO.getAttendeesCount() == null) {
			activityVO.setAttendeesCount(0);
		}

		if (activityVO.getActivityStatus() == null) {
			activityVO.setActivityStatus((byte) 0);
		}

		if (result.hasErrors() || imageError != null) {
			addFakeLoginMember(model, session);
			model.addAttribute("typeListData", activityTypeSvc.getAll());
			model.addAttribute("imageError", imageError);
			model.addAttribute("sourceActivityId", sourceActivityId);
			model.addAttribute("formTitle", sourceActivityId == null ? "我要辦活動" : "再次舉辦活動");
			model.addAttribute("submitText", sourceActivityId == null ? "建立活動" : "建立新場次");
			model.addAttribute("cancelUrl",
					sourceActivityId == null ? "/activity/front/myHostActivity" : "/activity/front/myHostActivity");
			model.addAttribute("cancelText", "返回我舉辦的活動");
			model.addAttribute("previewImageUrl", sourceActivityId == null ? "/images/activity/default-activity.jpg"
					: "/activity/front/image?id=" + sourceActivityId);
			return "front-end/activity/frontAddHostActivity";
		}

		activitySvc.saveActivity(activityVO);

		return "redirect:/activity/front/myHostActivity";
	}

	@GetMapping("/activity/front/myHostActivity")
	public String myHostActivity(Model model, HttpSession session) {
		Integer loginMemberId = getLoginMemberId(session);
		if (loginMemberId == null) {
			return "redirect:/activity/front/home?loginRequired=true";
		}
		List<ActivityVO> activityList = activitySvc.getActivitiesByMemberId(loginMemberId);
		Map<Integer, Integer> pendingReviewCountMap = new HashMap<>();
		Map<Integer, Boolean> canCancelActivityMap = new HashMap<>();
		Map<Integer, Boolean> endedActivityMap = new HashMap<>();
		LocalDateTime now = LocalDateTime.now();

		for (ActivityVO activityVO : activityList) {
			pendingReviewCountMap.put(activityVO.getActivityId(),
					activityOrderSvc.getPendingReviewCount(activityVO.getActivityId()));
			boolean hasEnded = activityVO.getEndTime() != null && !activityVO.getEndTime().isAfter(now);
			endedActivityMap.put(activityVO.getActivityId(), hasEnded);
			canCancelActivityMap.put(activityVO.getActivityId(),
					activityVO.getActivityStatus() != null && activityVO.getActivityStatus() != 2
							&& !hasEnded);
		}

		addFakeLoginMember(model, session);
		model.addAttribute("activityListData", activityList);
		model.addAttribute("pendingReviewCountMap", pendingReviewCountMap);
		model.addAttribute("canCancelActivityMap", canCancelActivityMap);
		model.addAttribute("endedActivityMap", endedActivityMap);

		return "front-end/activity/myHostActivity";
	}

	@PostMapping("/activity/front/cancelHostActivity")
	public String cancelHostActivity(@RequestParam("activityId") Integer activityId, HttpSession session) {
		Integer loginMemberId = getLoginMemberId(session);
		if (loginMemberId == null) {
			return "redirect:/activity/front/home?loginRequired=true";
		}

		ActivityVO activityVO = activitySvc.getOneActivity(activityId);
		if (activityVO == null) {
			return "redirect:/activity/front/myHostActivity?activityNotFound=true";
		}
		if (!loginMemberId.equals(activityVO.getMemberId())) {
			return "redirect:/activity/front/myHostActivity?noPermission=true";
		}

		if (!activitySvc.cancelActivityByHost(activityId, loginMemberId)) {
			return "redirect:/activity/front/myHostActivity?cannotCancel=true";
		}

		return "redirect:/activity/front/myHostActivity?cancelSuccess=true";
	}

	@GetMapping("/activity/front/memberList")
	public String memberList(@RequestParam("activityId") Integer activityId, Model model, HttpSession session) {
		Integer loginMemberId = getLoginMemberId(session);
		if (loginMemberId == null) {
			return "redirect:/activity/front/home?loginRequired=true";
		}
		addFakeLoginMember(model, session);

		ActivityVO activityVO = activitySvc.getOneActivity(activityId);
		if (activityVO == null) {
			return "redirect:/activity/front/myHostActivity";
		}
		if (!activityVO.getMemberId().equals(loginMemberId)) {
			return "redirect:/activity/front/myHostActivity?noPermission=true";
		}

		model.addAttribute("activityVO", activityVO);
		List<ActivityOrderVO> orderList = activityOrderSvc.getOrdersByActivityId(activityId);
		model.addAttribute("orderListData", orderList);
		model.addAttribute("orderCount", orderList.size());
		model.addAttribute("latestOrderId", getLatestOrderId(orderList));
		model.addAttribute("shouldAutoRefreshOrder", hasOrderStatus(orderList, (byte) 2)
				|| hasOrderStatus(orderList, (byte) 3));
		model.addAttribute("hasReachedMinimum", activitySvc.hasReachedMinimum(activityVO));
		model.addAttribute("isFull", activitySvc.isFull(activityVO));

		return "front-end/activity/frontActivityMemberList";
	}

	@GetMapping("/activity/front/memberList/summary")
	@ResponseBody
	public Map<String, Object> memberListSummary(@RequestParam("activityId") Integer activityId, HttpSession session) {
		Map<String, Object> summary = new HashMap<>();
		Integer loginMemberId = getLoginMemberId(session);
		ActivityVO activityVO = activitySvc.getOneActivity(activityId);

		if (loginMemberId == null || activityVO == null || !loginMemberId.equals(activityVO.getMemberId())) {
			summary.put("success", false);
			return summary;
		}

		List<ActivityOrderVO> orderList = activityOrderSvc.getOrdersByActivityId(activityId);
		summary.put("success", true);
		summary.put("orderCount", orderList.size());
		summary.put("latestOrderId", getLatestOrderId(orderList));
		summary.put("pendingReviewCount", activityOrderSvc.getPendingReviewCount(activityId));
		return summary;
	}

	@PostMapping("/activity/front/memberList/approve")
	public String approveMemberOrder(@RequestParam("activityOrderId") Integer activityOrderId, HttpSession session) {
		if (!isLoginMember(session)) {
			return "redirect:/activity/front/home?loginRequired=true";
		}

		ActivityOrderVO orderVO = activityOrderSvc.getOneOrder(activityOrderId);

		if (orderVO == null) {
			return "redirect:/activity/front/myHostActivity";
		}

		if (!isLoginMemberActivityHost(orderVO.getActivityId(), session)) {
			return "redirect:/activity/front/myHostActivity?noPermission=true";
		}

		if (isActivityReviewExpired(orderVO.getActivityId())) {
			return "redirect:/activity/front/memberList?activityId=" + orderVO.getActivityId()
					+ "&approveExpired=true";
		}

		ActivityOrderVO approvedOrder = activityOrderSvc.approveOrderByHost(activityOrderId);
		activitySvc.syncAttendeesFromOrders(orderVO.getActivityId());
		activityNotificationSvc.notifyBuyerApproved(activitySvc.getOneActivity(orderVO.getActivityId()), approvedOrder);
		return "redirect:/activity/front/memberList?activityId=" + orderVO.getActivityId() + "&approveSuccess=true";
	}

	@PostMapping("/activity/front/memberList/approve/ajax")
	@ResponseBody
	public Map<String, Object> approveMemberOrderAjax(@RequestParam("activityOrderId") Integer activityOrderId,
			HttpSession session) {
		return handleMemberOrderReviewAjax(activityOrderId, true, session);
	}

	@PostMapping("/activity/front/memberList/reject")
	public String rejectMemberOrder(@RequestParam("activityOrderId") Integer activityOrderId, HttpSession session) {
		if (!isLoginMember(session)) {
			return "redirect:/activity/front/home?loginRequired=true";
		}

		ActivityOrderVO orderVO = activityOrderSvc.getOneOrder(activityOrderId);

		if (orderVO == null) {
			return "redirect:/activity/front/myHostActivity";
		}

		if (!isLoginMemberActivityHost(orderVO.getActivityId(), session)) {
			return "redirect:/activity/front/myHostActivity?noPermission=true";
		}

		ActivityOrderVO rejectedOrder = activityOrderSvc.rejectOrderByHost(activityOrderId);
		activitySvc.syncAttendeesFromOrders(orderVO.getActivityId());
		activityNotificationSvc.notifyBuyerRejected(activitySvc.getOneActivity(orderVO.getActivityId()), rejectedOrder);
		return "redirect:/activity/front/memberList?activityId=" + orderVO.getActivityId() + "&rejectSuccess=true";
	}

	@PostMapping("/activity/front/memberList/reject/ajax")
	@ResponseBody
	public Map<String, Object> rejectMemberOrderAjax(@RequestParam("activityOrderId") Integer activityOrderId,
			HttpSession session) {
		return handleMemberOrderReviewAjax(activityOrderId, false, session);
	}

	@GetMapping("/activity/front/updateHostActivity")
	public String updateHostActivity(@RequestParam("id") Integer activityId, Model model, HttpSession session) {
		Integer loginMemberId = getLoginMemberId(session);
		if (loginMemberId == null) {
			return "redirect:/activity/front/home?loginRequired=true";
		}

		ActivityVO activityVO = activitySvc.getOneActivity(activityId);

		if (activityVO == null) {
			return "redirect:/activity/front/myHostActivity";
		}

		if (!activityVO.getMemberId().equals(loginMemberId)) {
			return "redirect:/activity/front/myHostActivity?noPermission=true";
		}

		if (activityVO.getAttendeesCount() != null && activityVO.getAttendeesCount() > 0) {
			return "redirect:/activity/front/myHostActivity?cannotEdit=true";
		}

		addFakeLoginMember(model, session);
		model.addAttribute("activityVO", activityVO);
		model.addAttribute("typeListData", activityTypeSvc.getAll());

		return "front-end/activity/frontUpdateHostActivity";
	}

	@PostMapping("/activity/front/updateHostActivity")
	public String saveUpdateHostActivity(@Valid @ModelAttribute("activityVO") ActivityVO formVO, BindingResult result,
			@RequestParam(value = "activityImageFile", required = false) MultipartFile activityImageFile, Model model,
			HttpSession session) throws IOException {
		Integer loginMemberId = getLoginMemberId(session);
		if (loginMemberId == null) {
			return "redirect:/activity/front/home?loginRequired=true";
		}

		ActivityVO oldVO = activitySvc.getOneActivity(formVO.getActivityId());

		if (oldVO == null) {
			return "redirect:/activity/front/myHostActivity";
		}

		if (!oldVO.getMemberId().equals(loginMemberId)) {
			return "redirect:/activity/front/myHostActivity?noPermission=true";
		}

		if (oldVO.getAttendeesCount() != null && oldVO.getAttendeesCount() > 0) {
			return "redirect:/activity/front/myHostActivity?cannotEdit=true";
		}

		String imageError = applyActivityImage(formVO, activityImageFile);
		if (!hasUploadedImage(activityImageFile)) {
			formVO.setActivityImage(oldVO.getActivityImage());
			formVO.setActivityImageType(oldVO.getActivityImageType());
		}

		if (result.hasErrors() || imageError != null) {
			addFakeLoginMember(model, session);
			model.addAttribute("typeListData", activityTypeSvc.getAll());
			model.addAttribute("imageError", imageError);
			return "front-end/activity/frontUpdateHostActivity";
		}

		formVO.setMemberId(oldVO.getMemberId());
		formVO.setAttendeesCount(oldVO.getAttendeesCount());
		formVO.setActivityStatus(oldVO.getActivityStatus());
		formVO.setCreatedAt(oldVO.getCreatedAt());

		activitySvc.saveActivity(formVO);

		return "redirect:/activity/front/myHostActivity";
	}

	private void addFakeLoginMember(Model model, HttpSession session) {
		Integer loginMemberId = getLoginMemberId(session);
		MemberVO loginMember = getSessionMember(session);

		model.addAttribute("loginMemberId", loginMemberId);
		model.addAttribute("loginMemberName", resolveLoginMemberName(loginMember, loginMemberId));
		model.addAttribute("isLoginMember", loginMemberId != null);
		model.addAttribute("memberListData", TEST_MEMBER_IDS);
	}

	private String applyActivityImage(ActivityVO activityVO, MultipartFile activityImageFile) throws IOException {
		if (!hasUploadedImage(activityImageFile)) {
			return null;
		}

		String contentType = activityImageFile.getContentType();
		if (!"image/jpeg".equals(contentType) && !"image/png".equals(contentType)) {
			return "活動圖片只能上傳 JPG 或 PNG 格式";
		}

		activityVO.setActivityImage(activityImageFile.getBytes());
		activityVO.setActivityImageType(contentType);
		return null;
	}

	private void applyRepeatedActivityImage(ActivityVO activityVO, Integer sourceActivityId,
			MultipartFile activityImageFile, HttpSession session) {
		if (sourceActivityId == null || hasUploadedImage(activityImageFile)) {
			return;
		}

		ActivityVO sourceVO = activitySvc.getOneActivity(sourceActivityId);
		Integer loginMemberId = getLoginMemberId(session);
		if (sourceVO == null || !sourceVO.getMemberId().equals(loginMemberId)) {
			return;
		}

		activityVO.setActivityImage(sourceVO.getActivityImage());
		activityVO.setActivityImageType(sourceVO.getActivityImageType());
	}

	private boolean hasUploadedImage(MultipartFile activityImageFile) {
		return activityImageFile != null && !activityImageFile.isEmpty();
	}

	private boolean hasOrderStatus(List<ActivityOrderVO> orderList, byte orderStatus) {
		for (ActivityOrderVO orderVO : orderList) {
			if (orderVO.getOrderStatus() != null && orderVO.getOrderStatus() == orderStatus) {
				return true;
			}
		}
		return false;
	}

	private boolean isLoginMemberActivityHost(Integer activityId, HttpSession session) {
		ActivityVO activityVO = activitySvc.getOneActivity(activityId);
		Integer loginMemberId = getLoginMemberId(session);
		return activityVO != null && activityVO.getMemberId().equals(loginMemberId);
	}

	private boolean isActivityReviewExpired(Integer activityId) {
		ActivityVO activityVO = activitySvc.getOneActivity(activityId);
		return activityVO == null || (activityVO.getRegistrationDeadline() != null
				&& LocalDateTime.now().isAfter(activityVO.getRegistrationDeadline()));
	}

	private boolean canViewActivityOrderStatus(ActivityOrderVO orderVO, Integer loginMemberId) {
		if (orderVO == null || loginMemberId == null) {
			return false;
		}
		if (loginMemberId.equals(orderVO.getBuyerMemberId())) {
			return true;
		}
		ActivityVO activityVO = activitySvc.getOneActivity(orderVO.getActivityId());
		return activityVO != null && loginMemberId.equals(activityVO.getMemberId());
	}

	private Map<String, Object> activityOrderActionResult(boolean success, String message, ActivityOrderVO orderVO) {
		Map<String, Object> result = new HashMap<>();
		result.put("success", success);
		result.put("message", message);
		if (orderVO != null) {
			result.put("activityOrderId", orderVO.getActivityOrderId());
			result.put("orderStatus", orderVO.getOrderStatus());
			result.put("refundStatus", orderVO.getRefundStatus());
		}
		return result;
	}

	private Map<String, Object> handleMemberOrderReviewAjax(Integer activityOrderId, boolean approve,
			HttpSession session) {
		if (!isLoginMember(session)) {
			return activityOrderActionResult(false, "loginRequired", null);
		}

		ActivityOrderVO orderVO = activityOrderSvc.getOneOrder(activityOrderId);
		if (orderVO == null) {
			return activityOrderActionResult(false, "notFound", null);
		}

		if (!isLoginMemberActivityHost(orderVO.getActivityId(), session)) {
			return activityOrderActionResult(false, "noPermission", orderVO);
		}

		if (approve && isActivityReviewExpired(orderVO.getActivityId())) {
			return activityOrderActionResult(false, "reviewExpired", orderVO);
		}

		ActivityOrderVO reviewedOrder;
		ActivityVO activityVO = activitySvc.getOneActivity(orderVO.getActivityId());
		if (approve) {
			reviewedOrder = activityOrderSvc.approveOrderByHost(activityOrderId);
			activityNotificationSvc.notifyBuyerApproved(activityVO, reviewedOrder);
		} else {
			reviewedOrder = activityOrderSvc.rejectOrderByHost(activityOrderId);
			activityNotificationSvc.notifyBuyerRejected(activityVO, reviewedOrder);
		}
		activitySvc.syncAttendeesFromOrders(orderVO.getActivityId());
		return activityOrderActionResult(true, approve ? "approved" : "rejected", reviewedOrder);
	}

	private Integer parseInteger(String value) {
		if (value == null || value.trim().isEmpty()) {
			return null;
		}
		try {
			return Integer.valueOf(value.trim());
		} catch (NumberFormatException e) {
			return null;
		}
	}

	private Integer getLatestOrderId(List<ActivityOrderVO> orderList) {
		Integer latestOrderId = 0;
		if (orderList == null) {
			return latestOrderId;
		}
		for (ActivityOrderVO orderVO : orderList) {
			if (orderVO != null && orderVO.getActivityOrderId() != null
					&& orderVO.getActivityOrderId() > latestOrderId) {
				latestOrderId = orderVO.getActivityOrderId();
			}
		}
		return latestOrderId;
	}

	private Map<Integer, String> buildRegistrationStatusMap(List<ActivityVO> activityList) {
		Map<Integer, String> statusMap = new HashMap<>();
		for (ActivityVO activityVO : activityList) {
			statusMap.put(activityVO.getActivityId(), getRegistrationStatusText(activityVO));
		}
		return statusMap;
	}

	private String getRegistrationStatusText(ActivityVO activityVO) {
		if (activityVO == null) {
			return "活動不存在";
		}

		LocalDateTime now = LocalDateTime.now();
		if (activityVO.getEndTime() != null && now.isAfter(activityVO.getEndTime())) {
			return "活動已結束";
		}

		if (activityVO.getRegistrationStartTime() != null && now.isBefore(activityVO.getRegistrationStartTime())) {
			return "尚未開放報名";
		}

		if (activityVO.getRegistrationDeadline() != null && now.isAfter(activityVO.getRegistrationDeadline())) {
			return "報名已截止";
		}

		if (activityVO.getRegistrationDeadline() != null
				&& Duration.between(now, activityVO.getRegistrationDeadline()).toHours() <= 168) {
			return "即將截止";
		}

		return "報名中";
	}

	private Integer getLoginMemberId(HttpSession session) {
		MemberVO loginMember = getSessionMember(session);
		if (loginMember != null && loginMember.getMemberId() != null) {
			return loginMember.getMemberId();
		}

		Object memberId = session.getAttribute(ACTIVITY_LOGIN_MEMBER_ID);
		if (memberId instanceof Integer && TEST_MEMBER_IDS.contains((Integer) memberId)) {
			return (Integer) memberId;
		}

		return null;
	}

	private MemberVO getSessionMember(HttpSession session) {
		Object member = session.getAttribute("memberVO");
		if (member instanceof MemberVO) {
			return (MemberVO) member;
		}

		return null;
	}

	private String resolveLoginMemberName(MemberVO loginMember, Integer loginMemberId) {
		if (loginMemberId == null) {
			return "未登入";
		}

		if (loginMember != null && loginMember.getNickname() != null && !loginMember.getNickname().trim().isEmpty()) {
			return loginMember.getNickname();
		}

		return "會員" + loginMemberId;
	}

	private boolean isLoginMember(HttpSession session) {
		return getLoginMemberId(session) != null;
	}
}
