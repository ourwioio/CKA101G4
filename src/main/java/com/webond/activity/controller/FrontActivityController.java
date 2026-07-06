package com.webond.activity.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.webond.activity.model.ActivityOrderService;
import com.webond.activity.model.ActivityOrderVO;
import com.webond.activity.model.ActivityService;
import com.webond.activity.model.ActivityTypeService;
import com.webond.activity.model.ActivityVO;
import com.webond.member.model.MemberVO;
import com.webond.member.repository.MemberRepository;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
public class FrontActivityController {

	private static final Integer DEFAULT_FAKE_LOGIN_MEMBER_ID = 1;
	private static final String ACTIVITY_LOGIN_MEMBER_ID = "activityLoginMemberId";

	@Autowired
	private ActivityService activitySvc;

	@Autowired
	private ActivityOrderService activityOrderSvc;

	@Autowired
	private ActivityTypeService activityTypeSvc;

	@Autowired
	private MemberRepository memberRepo;

	@GetMapping("/activity/front/home")
	public String frontActHome(Model model, HttpSession session) {
		addFakeLoginMember(model, session);
		return "front-end/activity/frontActHome";
	}

	@PostMapping("/activity/front/fakeLogin")
	public String switchFakeLogin(@RequestParam("memberId") Integer memberId, HttpSession session) {
		if (memberId != null && memberRepo.existsById(memberId)) {
			session.setAttribute(ACTIVITY_LOGIN_MEMBER_ID, memberId);
		}
		return "redirect:/activity/front/home";
	}

	@GetMapping("/activity/front/list")
	public String frontActivityList(@RequestParam(value = "keyword", required = false) String keyword,
			@RequestParam(value = "typeId", required = false) Integer typeId,
			@RequestParam(value = "status", required = false) Byte status,
			@RequestParam(value = "onlyAvailable", required = false) Boolean onlyAvailable,
			@RequestParam(value = "sort", required = false) String sort, Model model, HttpSession session) {

		addFakeLoginMember(model, session);
		model.addAttribute("activityListData", activitySvc.searchActivities(keyword, typeId, status, onlyAvailable, sort));
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
			@RequestParam(value = "full", required = false) Boolean full, Model model, HttpSession session) {

		addFakeLoginMember(model, session);

		ActivityVO activityVO = activitySvc.getOneActivity(activityId);
		model.addAttribute("activityVO", activityVO);

		if (Boolean.TRUE.equals(full)) {
			model.addAttribute("fullMessage", "Activity is full.");
		}

		return "front-end/activity/frontActivityDetail";
	}

	@GetMapping("/activity/front/order")
	public String frontActivityOrder(@RequestParam("activityId") Integer activityId, Model model, HttpSession session) {
		Integer loginMemberId = getLoginMemberId(session);
		addFakeLoginMember(model, session);

		ActivityVO activityVO = activitySvc.getOneActivity(activityId);

		ActivityOrderVO orderVO = new ActivityOrderVO();
		orderVO.setActivityId(activityVO.getActivityId());
		orderVO.setBuyerMemberId(loginMemberId);
		orderVO.setBookingCount(1);
		orderVO.setActivityPrice(activityVO.getActivityPrice());
		orderVO.setTotalAmount(activityVO.getActivityPrice());
		orderVO.setOrderStatus((byte) 0);

		model.addAttribute("activityVO", activityVO);
		model.addAttribute("activityOrderVO", orderVO);

		return "front-end/activity/frontActivityOrder";
	}

	@PostMapping("/activity/front/order/save")
	public String saveFrontOrder(@ModelAttribute("activityOrderVO") ActivityOrderVO orderVO, HttpSession session) {
		Integer bookingCount = orderVO.getBookingCount();
		Integer activityPrice = orderVO.getActivityPrice();

		if (bookingCount == null || bookingCount < 1) {
			bookingCount = 1;
		}

		orderVO.setBuyerMemberId(getLoginMemberId(session));
		orderVO.setBookingCount(bookingCount);
		orderVO.setTotalAmount(activityPrice * bookingCount);
		orderVO.setOrderStatus((byte) 0);

		if (!activitySvc.canRegister(orderVO.getActivityId(), orderVO.getBookingCount())) {
			return "redirect:/activity/front/detail?id=" + orderVO.getActivityId() + "&full=true";
		}

		activityOrderSvc.addOrder(orderVO);
		activitySvc.increaseAttendees(orderVO.getActivityId(), orderVO.getBookingCount());

		return "redirect:/activity/front/myOrder";
	}

	@GetMapping("/activity/front/myOrder")
	public String myOrder(Model model, HttpSession session) {
		Integer loginMemberId = getLoginMemberId(session);

		addFakeLoginMember(model, session);
		model.addAttribute("orderListData", activityOrderSvc.getOrdersByBuyerMemberId(loginMemberId));
		model.addAttribute("activityListData", activitySvc.getAll());

		return "front-end/activity/myActivityOrder";
	}

	@GetMapping("/activity/front/addHostActivity")
	public String addHostActivity(Model model, HttpSession session) {
		Integer loginMemberId = getLoginMemberId(session);
		ActivityVO activityVO = new ActivityVO();

		activityVO.setMemberId(loginMemberId);
		activityVO.setAttendeesCount(0);
		activityVO.setActivityStatus((byte) 0);

		addFakeLoginMember(model, session);
		model.addAttribute("activityVO", activityVO);
		model.addAttribute("typeListData", activityTypeSvc.getAll());

		return "front-end/activity/frontAddHostActivity";
	}

	@PostMapping("/activity/front/insertHostActivity")
	public String insertHostActivity(@Valid @ModelAttribute("activityVO") ActivityVO activityVO, BindingResult result,
			Model model, HttpSession session) {

		activityVO.setMemberId(getLoginMemberId(session));

		if (activityVO.getAttendeesCount() == null) {
			activityVO.setAttendeesCount(0);
		}

		if (activityVO.getActivityStatus() == null) {
			activityVO.setActivityStatus((byte) 0);
		}

		if (result.hasErrors()) {
			addFakeLoginMember(model, session);
			model.addAttribute("typeListData", activityTypeSvc.getAll());
			return "front-end/activity/frontAddHostActivity";
		}

		activitySvc.saveActivity(activityVO);

		return "redirect:/activity/front/myHostActivity";
	}

	@GetMapping("/activity/front/myHostActivity")
	public String myHostActivity(Model model, HttpSession session) {
		Integer loginMemberId = getLoginMemberId(session);

		addFakeLoginMember(model, session);
		model.addAttribute("activityListData", activitySvc.getActivitiesByMemberId(loginMemberId));

		return "front-end/activity/myHostActivity";
	}

	@GetMapping("/activity/front/memberList")
	public String memberList(@RequestParam("activityId") Integer activityId, Model model, HttpSession session) {
		addFakeLoginMember(model, session);

		ActivityVO activityVO = activitySvc.getOneActivity(activityId);
		model.addAttribute("activityVO", activityVO);
		model.addAttribute("orderListData", activityOrderSvc.getOrdersByActivityId(activityId));

		return "front-end/activity/frontActivityMemberList";
	}

	@GetMapping("/activity/front/updateHostActivity")
	public String updateHostActivity(@RequestParam("id") Integer activityId, Model model, HttpSession session) {
		Integer loginMemberId = getLoginMemberId(session);

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
			Model model, HttpSession session) {
		Integer loginMemberId = getLoginMemberId(session);

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

		if (result.hasErrors()) {
			addFakeLoginMember(model, session);
			model.addAttribute("typeListData", activityTypeSvc.getAll());
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
		MemberVO loginMember = memberRepo.findById(loginMemberId).orElse(null);
		String loginMemberName = loginMember != null && loginMember.getNickname() != null
				&& !loginMember.getNickname().trim().isEmpty() ? loginMember.getNickname() : "Member " + loginMemberId;

		model.addAttribute("loginMemberId", loginMemberId);
		model.addAttribute("loginMemberName", loginMemberName);
		model.addAttribute("memberListData", memberRepo.findAll(Sort.by(Sort.Direction.ASC, "memberId")));
	}

	private Integer getLoginMemberId(HttpSession session) {
		Object memberId = session.getAttribute(ACTIVITY_LOGIN_MEMBER_ID);
		if (memberId instanceof Integer && memberRepo.existsById((Integer) memberId)) {
			return (Integer) memberId;
		}

		Integer defaultMemberId = memberRepo.findAll(Sort.by(Sort.Direction.ASC, "memberId")).stream()
				.map(MemberVO::getMemberId)
				.findFirst()
				.orElse(DEFAULT_FAKE_LOGIN_MEMBER_ID);
		session.setAttribute(ACTIVITY_LOGIN_MEMBER_ID, defaultMemberId);
		return defaultMemberId;
	}
}
