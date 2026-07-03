package com.webond.member.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.webond.member.model.NotificationVO;
import com.webond.member.service.NotificationService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/notification")
public class NotificationController {

	@Autowired
	NotificationService notificationService;

	@GetMapping("listAllNotification")
	public String listAllNotification(ModelMap model) {
		List<NotificationVO> list = notificationService.getAll();
		model.addAttribute("notificationListData", list);
		return "back-end/member/listAllNotification";
	}

	@PostMapping("getOne_For_update")
	public String getOneEmpNotification(@RequestParam("notificationId") Integer notificationId, ModelMap model) {
		NotificationVO notificationVO = notificationService.getOneNotification(notificationId);
		model.addAttribute("notificationVO", notificationVO);
		return "back-end/member/updateNotification";
	}

	@PostMapping("update")
	public String update(@Valid NotificationVO notificationVO, BindingResult result, ModelMap model) {
		if (result.hasErrors()) {
			return "back-end/member/updateNotification";
		}
		notificationService.updateNotification(notificationVO);
		model.addAttribute("success", "finish");

		return "redirect:/notification/listAllNotification";

	}

	@PostMapping("delete")
	public String delete(@RequestParam("notificationId") String notificationId, ModelMap model) {
		notificationService.deleteNotification(Integer.valueOf(notificationId));

		List<NotificationVO> list = notificationService.getAll();
		model.addAttribute("notificationListData", list);
		model.addAttribute("success", "刪除成功");
		return "back-end/member/listAllNotification";
	}
	
	@GetMapping("addNotification")
	public String addNotification(ModelMap model) {
		NotificationVO notificationVO = new NotificationVO();
		model.addAttribute("notificationVO", notificationVO);
		return "back-end/member/addNotification";
	}
	
	@PostMapping("insert")
	public String insert(@Valid NotificationVO notificationVO, BindingResult result, ModelMap model) {
		
		if(notificationVO.getMember() == null) {
			model.addAttribute("memberError", "請選擇會員");
			return "back-end/member/addNotification";
		}
		
		if (result.hasErrors()) {
			return "back-end/member/addNotification";
		}
		notificationService.addNotification(notificationVO);
		model.addAttribute("success", "finish");

		return "redirect:/notification/listAllNotification";

	}
	

}
