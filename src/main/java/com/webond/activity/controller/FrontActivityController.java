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
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.webond.activity.model.ActivityOrderService;
import com.webond.activity.model.ActivityOrderVO;
import com.webond.activity.model.ActivityService;
import com.webond.activity.model.ActivityTypeService;
import com.webond.activity.model.ActivityVO;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
public class FrontActivityController {

	private static final String ACTIVITY_LOGIN_MEMBER_ID = "activityLoginMemberId";
	private static final List<Integer> TEST_MEMBER_IDS = Arrays.asList(1, 2, 3);
	private static final String DEFAULT_ACTIVITY_IMAGE_PATH = "static/images/activity/default-activity.jpg";

	@Autowired
	private ActivityService activitySvc;

	@Autowired
	private ActivityOrderService activityOrderSvc;

	@Autowired
	private ActivityTypeService activityTypeSvc;

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
		model.addAttribute("activityVO", activityVO);
		model.addAttribute("registrationStatusText", getRegistrationStatusText(activityVO));
		model.addAttribute("backUrl", resolveDetailBackUrl(from));
		model.addAttribute("backText", resolveDetailBackText(from));
		model.addAttribute("showOrderButton", isLoginMember(session) && (from == null || from.trim().isEmpty()));
		model.addAttribute("showLoginPrompt", !isLoginMember(session) && (from == null || from.trim().isEmpty()));

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
		if (activityId != null) {
			ActivityVO activityVO = activitySvc.getOneActivity(activityId);
			if (activityVO != null && activityVO.getActivityImage() != null
					&& activityVO.getActivityImage().length > 0) {
				String imageType = activityVO.getActivityImageType() == null ? "image/jpeg"
						: activityVO.getActivityImageType();
				return ResponseEntity.ok()
						.contentType(MediaType.parseMediaType(imageType))
						.body(activityVO.getActivityImage());
			}
		}

		ClassPathResource defaultImage = new ClassPathResource(DEFAULT_ACTIVITY_IMAGE_PATH);
		return ResponseEntity.ok()
				.contentType(MediaType.IMAGE_JPEG)
				.body(defaultImage.getInputStream().readAllBytes());
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
		orderVO.setOrderStatus((byte) 3);
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

		if (loginMemberId.equals(activityVO.getMemberId())) {
			return "redirect:/activity/front/detail?id=" + orderVO.getActivityId() + "&owner=true";
		}

		if (!activitySvc.isRegistrationOpen(orderVO.getActivityId())) {
			return "redirect:/activity/front/detail?id=" + orderVO.getActivityId() + "&closed=true";
		}

		orderVO.setBuyerMemberId(loginMemberId);
		orderVO.setBookingCount(bookingCount);
		orderVO.setTotalAmount(activityPrice * bookingCount);
		orderVO.setOrderStatus((byte) 3);

		if (activityOrderSvc.hasActiveOrder(orderVO.getActivityId(), loginMemberId)) {
			return "redirect:/activity/front/detail?id=" + orderVO.getActivityId() + "&duplicate=true";
		}

		if (!activitySvc.canRegister(orderVO.getActivityId(), orderVO.getBookingCount())) {
			return "redirect:/activity/front/detail?id=" + orderVO.getActivityId() + "&full=true";
		}

		activityOrderSvc.addOrder(orderVO);

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

		if (!activitySvc.canRegister(orderVO.getActivityId(), orderVO.getBookingCount())) {
			return "redirect:/activity/front/myOrder?cannotPay=true";
		}

		ActivityOrderVO paidOrder = activityOrderSvc.payOrder(activityOrderId, getLoginMemberId(session),
				activityPaymentMethod);

		if (paidOrder != null) {
			activitySvc.syncAttendeesFromOrders(paidOrder.getActivityId());
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
		model.addAttribute("shouldAutoRefreshOrder", hasOrderStatus(orderList, (byte) 3)
				|| hasOrderStatus(orderList, (byte) 4));

		return "front-end/activity/myActivityOrder";
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
		model.addAttribute("cancelUrl", "/activity/front/home");
		model.addAttribute("cancelText", "返回使用者首頁");
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
					sourceActivityId == null ? "/activity/front/home" : "/activity/front/myHostActivity");
			model.addAttribute("cancelText", sourceActivityId == null ? "返回使用者首頁" : "返回我舉辦的活動");
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

		for (ActivityVO activityVO : activityList) {
			pendingReviewCountMap.put(activityVO.getActivityId(),
					activityOrderSvc.getPendingReviewCount(activityVO.getActivityId()));
		}

		addFakeLoginMember(model, session);
		model.addAttribute("activityListData", activityList);
		model.addAttribute("pendingReviewCountMap", pendingReviewCountMap);

		return "front-end/activity/myHostActivity";
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
		model.addAttribute("shouldAutoRefreshOrder", hasOrderStatus(orderList, (byte) 3)
				|| hasOrderStatus(orderList, (byte) 4));
		model.addAttribute("hasReachedMinimum", activitySvc.hasReachedMinimum(activityVO));
		model.addAttribute("isFull", activitySvc.isFull(activityVO));

		return "front-end/activity/frontActivityMemberList";
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

		if (!activitySvc.canRegister(orderVO.getActivityId(), orderVO.getBookingCount())) {
			return "redirect:/activity/front/memberList?activityId=" + orderVO.getActivityId() + "&approveFailed=true";
		}

		activityOrderSvc.approveOrderByHost(activityOrderId);
		activitySvc.syncAttendeesFromOrders(orderVO.getActivityId());
		return "redirect:/activity/front/memberList?activityId=" + orderVO.getActivityId() + "&approveSuccess=true";
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

		activityOrderSvc.rejectOrderByHost(activityOrderId);
		activitySvc.syncAttendeesFromOrders(orderVO.getActivityId());
		return "redirect:/activity/front/memberList?activityId=" + orderVO.getActivityId() + "&rejectSuccess=true";
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

		model.addAttribute("loginMemberId", loginMemberId);
		model.addAttribute("loginMemberName", loginMemberId == null ? "未登入" : "會員" + loginMemberId);
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
				&& Duration.between(now, activityVO.getRegistrationDeadline()).toHours() <= 24) {
			return "即將截止";
		}

		return "報名中";
	}

	private Integer getLoginMemberId(HttpSession session) {
		Object memberId = session.getAttribute(ACTIVITY_LOGIN_MEMBER_ID);
		if (memberId instanceof Integer && TEST_MEMBER_IDS.contains((Integer) memberId)) {
			return (Integer) memberId;
		}

		return null;
	}

	private boolean isLoginMember(HttpSession session) {
		return getLoginMemberId(session) != null;
	}
}
