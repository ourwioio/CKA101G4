package com.webond.activity.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.webond.activity.model.ActivityOrderService;
import com.webond.activity.model.ActivityService;
import com.webond.activity.model.ActivityVO;
import com.webond.activity.model.GroupChatMessageService;
import com.webond.member.model.MemberVO;

import jakarta.servlet.http.HttpSession;

@Controller
public class GroupChatController {

	private static final String ACTIVITY_LOGIN_MEMBER_ID = "activityLoginMemberId";

	@Autowired
	private GroupChatMessageService groupChatMessageSvc;

	@Autowired
	private ActivityService activitySvc;

	@Autowired
	private ActivityOrderService activityOrderSvc;

	@GetMapping("/activity/front/chat")
	public String chatRoom(@RequestParam("activityId") Integer activityId,
			@RequestParam(value = "from", required = false) String from, Model model, HttpSession session) {
		Integer loginMemberId = getLoginMemberId(session);
		ActivityVO activityVO = activitySvc.getOneActivity(activityId);

		if (!canEnterChat(activityVO, loginMemberId)) {
			return "redirect:" + resolveChatBackUrl(from) + "?chatDenied=true";
		}

		model.addAttribute("activityVO", activityVO);
		model.addAttribute("loginMemberId", loginMemberId);
		model.addAttribute("loginMemberName", resolveLoginMemberName(session, loginMemberId));
		model.addAttribute("messageListData", groupChatMessageSvc.getMessagesByActivityId(activityId));
		model.addAttribute("from", from);
		model.addAttribute("backUrl", resolveChatBackUrl(from));
		return "front-end/activity/groupChatRoom";
	}

	@PostMapping("/activity/front/chat/send")
	public String sendMessage(@RequestParam("activityId") Integer activityId,
			@RequestParam(value = "from", required = false) String from,
			@RequestParam("content") String content, HttpSession session) {
		Integer loginMemberId = getLoginMemberId(session);
		ActivityVO activityVO = activitySvc.getOneActivity(activityId);

		if (!canEnterChat(activityVO, loginMemberId)) {
			return "redirect:" + resolveChatBackUrl(from) + "?chatDenied=true";
		}

		groupChatMessageSvc.sendMessage(activityId, loginMemberId, content);
		return "redirect:/activity/front/chat?activityId=" + activityId + "&from=" + normalizeFrom(from);
	}

	private String resolveChatBackUrl(String from) {
		if ("host".equals(from)) {
			return "/activity/front/myHostActivity";
		}
		if ("order".equals(from)) {
			return "/activity/front/myOrder";
		}
		return "/activity/front/list";
	}

	private String normalizeFrom(String from) {
		if ("host".equals(from) || "order".equals(from)) {
			return from;
		}
		return "list";
	}

	private boolean canEnterChat(ActivityVO activityVO, Integer loginMemberId) {
		if (activityVO == null || loginMemberId == null) {
			return false;
		}
		if (loginMemberId.equals(activityVO.getMemberId())) {
			return true;
		}
		return activityOrderSvc.hasPaidOrder(activityVO.getActivityId(), loginMemberId);
	}

	private Integer getLoginMemberId(HttpSession session) {
		Object member = session.getAttribute("memberVO");
		if (member instanceof MemberVO) {
			return ((MemberVO) member).getMemberId();
		}

		Object memberId = session.getAttribute(ACTIVITY_LOGIN_MEMBER_ID);
		if (memberId instanceof Integer) {
			return (Integer) memberId;
		}

		return null;
	}

	private String resolveLoginMemberName(HttpSession session, Integer loginMemberId) {
		Object member = session.getAttribute("memberVO");
		if (member instanceof MemberVO) {
			MemberVO memberVO = (MemberVO) member;
			if (memberVO.getNickname() != null && !memberVO.getNickname().trim().isEmpty()) {
				return memberVO.getNickname();
			}
		}

		return "\u6703\u54e1" + loginMemberId;
	}
}
