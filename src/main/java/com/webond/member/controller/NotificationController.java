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

import com.webond.employee.model.EmployeeVO;
import com.webond.member.model.NotificationVO;
import com.webond.member.repository.MemberRepository;
import com.webond.member.service.MemberService;
import com.webond.member.service.NotificationService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/notification")
public class NotificationController {

	@Autowired
	NotificationService notificationService;

	@Autowired
	MemberService memberService;
	
	@Autowired 
	MemberRepository memberRepository;

//	@Autowired
//	EmployeeService employeeService;
//	
//	@ModelAttribute("empList")
//	public List<EmpVO> empListData() {
//	    return empService.getAll();
//	}

	@GetMapping("/fakeLogin")
	public String fakeLogin(HttpSession session) {
		session.setAttribute("loginEmployeeId", 1001);
		return "back-end/member/empPage";
	}

	@GetMapping("myNotification")
	public String myNotification(ModelMap model, HttpSession session) {
		Integer employeeId = (Integer) session.getAttribute("loginEmployeeId");
		if (employeeId == null) {
			return "redirect:/notification/fakeLogin";

		}
		List<NotificationVO> list = notificationService.getNotificationByEmployeeId(employeeId);

		model.addAttribute("notificationEmpListData", list);
		return "back-end/member/myNotification";

	}

	@GetMapping("listAllNotification")
	public String listAllNotification(ModelMap model) {
		List<NotificationVO> list = notificationService.getAll();
		model.addAttribute("notificationListData", list);
		return "back-end/member/listAllNotification";
	}

	@PostMapping("getOne_For_update")
	public String getOneEmpNotification(@RequestParam("notificationId") Integer notificationId, ModelMap model,
			HttpSession session) {

		Integer employeeId = (Integer) session.getAttribute("loginEmployeeId");
		if (employeeId == null) {
			return "redirect:/notification/fakeLogin";
		}
		NotificationVO notificationVO = notificationService.getOneForUpdate(notificationId);
		model.addAttribute("notificationVO", notificationVO);
		return "back-end/member/updateNotification";
	}

	@PostMapping("update")
	public String update(@Valid NotificationVO notificationVO, BindingResult result, ModelMap model,
			HttpSession session) {

		Integer employeeId = (Integer) session.getAttribute("loginEmployeeId");
		if (employeeId == null) {
			return "redirect:/notification/fakeLogin";
		}
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

		return "redirect:/notification/listAllNotification";

	}

	@GetMapping("addNotification")
	public String addNotification(ModelMap model, HttpSession session) {

		Integer employeeId = (Integer) session.getAttribute("loginEmployeeId");
		if (employeeId == null) {
			return "redirect:/notification/fakeLogin";
		}
		NotificationVO notificationVO = new NotificationVO();
		model.addAttribute("notificationVO", notificationVO);

		return "back-end/member/addNotification";
	}

	@PostMapping("insert")
	public String insert(@Valid NotificationVO notificationVO, BindingResult result, ModelMap model,
			HttpSession session) {

		Integer employeeId = (Integer) session.getAttribute("loginEmployeeId");
		if (employeeId == null) {
			return "redirect:/notification/fakeLogin";
		}
		
		EmployeeVO employee = new EmployeeVO();
		employee.setEmployeeId(employeeId);
		notificationVO.setEmployee(employee);
		

		if (result.hasFieldErrors("member.memberId")) {

		    model.addAttribute("memberError", "會員編號不存在");
			return "back-end/member/addNotification";
		}
		
		
		if (notificationVO.getMember() == null || notificationVO.getMember().getMemberId() == null) {
			model.addAttribute("memberError", "請填寫會員編號");
			return "back-end/member/addNotification";
		}

		boolean exists = memberRepository.existsById(notificationVO.getMember().getMemberId());

		if (!exists) {
			model.addAttribute("memberError", "會員編號不存在");
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
