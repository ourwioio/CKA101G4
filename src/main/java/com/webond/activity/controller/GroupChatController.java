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
	public String chatRoom(@RequestParam("activityId") Integer activityId, Model model, HttpSession session) {
		Integer loginMemberId = getLoginMemberId(session);
		ActivityVO activityVO = activitySvc.getOneActivity(activityId);

		if (!canEnterChat(activityVO, loginMemberId)) {
			return "redirect:/activity/front/home?chatDenied=true";
		}

		model.addAttribute("activityVO", activityVO);
		model.addAttribute("loginMemberId", loginMemberId);
		model.addAttribute("loginMemberName", "會員" + loginMemberId);
		model.addAttribute("messageListData", groupChatMessageSvc.getMessagesByActivityId(activityId));
		return "front-end/activity/groupChatRoom";
	}

	@PostMapping("/activity/front/chat/send")
	public String sendMessage(@RequestParam("activityId") Integer activityId,
			@RequestParam("content") String content, HttpSession session) {
		Integer loginMemberId = getLoginMemberId(session);
		ActivityVO activityVO = activitySvc.getOneActivity(activityId);

		if (!canEnterChat(activityVO, loginMemberId)) {
			return "redirect:/activity/front/home?chatDenied=true";
		}

		groupChatMessageSvc.sendMessage(activityId, loginMemberId, content);
		return "redirect:/activity/front/chat?activityId=" + activityId;
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
		Object memberId = session.getAttribute(ACTIVITY_LOGIN_MEMBER_ID);
		if (memberId instanceof Integer) {
			return (Integer) memberId;
		}

		return null;
	}
}
