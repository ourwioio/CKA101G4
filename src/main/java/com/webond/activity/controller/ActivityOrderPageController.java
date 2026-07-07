package com.webond.activity.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.webond.activity.model.ActivityOrderService;
import com.webond.activity.model.ActivityOrderVO;
import com.webond.activity.model.ActivityService;
import com.webond.employee.model.EmployeeVO;
import com.webond.employee.repository.EmployeeRepository;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/activityOrder")
public class ActivityOrderPageController {

	private static final String ACTIVITY_ADMIN_EMPLOYEE_ID = "activityAdminEmployeeId";

	@Autowired
	private ActivityOrderService orderSvc;

	@Autowired
	private ActivityService activitySvc;

	@Autowired
	private EmployeeRepository employeeRepo;

	@GetMapping("/listAllActivityOrder")
	public String listAllActivityOrder(Model model, HttpSession session) {
		model.addAttribute("orderListData", orderSvc.getAll());
		model.addAttribute("activityListData", activitySvc.getAll());
		addFakeEmployee(model, session);

		return "front-end/activityorder/listAllActivityOrder";
	}

	@PostMapping("/approve")
	public String approveOrder(@RequestParam("activityOrderId") Integer activityOrderId, HttpSession session) {
		return "redirect:/activityOrder/listAllActivityOrder?hostReviewOnly=true";
	}

	@PostMapping("/reject")
	public String rejectOrder(@RequestParam("activityOrderId") Integer activityOrderId, HttpSession session) {
		return "redirect:/activityOrder/listAllActivityOrder?hostReviewOnly=true";
	}

	private void addFakeEmployee(Model model, HttpSession session) {
		Integer employeeId = getLoginEmployeeId(session);
		EmployeeVO employeeVO = employeeId == null ? null : employeeRepo.findById(employeeId).orElse(null);
		String employeeName = employeeVO != null && employeeVO.getEmpName() != null
				&& !employeeVO.getEmpName().trim().isEmpty() ? employeeVO.getEmpName()
						: employeeId == null ? "No employee" : "Employee " + employeeId;

		model.addAttribute("loginEmployeeId", employeeId);
		model.addAttribute("loginEmployeeName", employeeName);
		model.addAttribute("employeeListData", employeeRepo.findAll(Sort.by(Sort.Direction.ASC, "employeeId")));
	}

	private Integer getLoginEmployeeId(HttpSession session) {
		Object employeeId = session.getAttribute(ACTIVITY_ADMIN_EMPLOYEE_ID);
		if (employeeId instanceof Integer && employeeRepo.existsById((Integer) employeeId)) {
			return (Integer) employeeId;
		}

		Integer defaultEmployeeId = employeeRepo.findAll(Sort.by(Sort.Direction.ASC, "employeeId")).stream()
				.map(EmployeeVO::getEmployeeId)
				.findFirst()
				.orElse(null);
		if (defaultEmployeeId != null) {
			session.setAttribute(ACTIVITY_ADMIN_EMPLOYEE_ID, defaultEmployeeId);
		}
		return defaultEmployeeId;
	}
}
