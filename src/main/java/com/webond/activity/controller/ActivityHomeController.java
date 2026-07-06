package com.webond.activity.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.webond.employee.model.EmployeeVO;
import com.webond.employee.repository.EmployeeRepository;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/activity")
public class ActivityHomeController {

	private static final String ACTIVITY_ADMIN_EMPLOYEE_ID = "activityAdminEmployeeId";

	@Autowired
	private EmployeeRepository employeeRepo;

	@GetMapping("/home")
	public String portal() {
		return "front-end/activity/activityPortal";
	}

	@GetMapping("/admin/home")
	public String adminHome(Model model, HttpSession session) {
		addFakeEmployee(model, session);
		return "front-end/activity/activityHome";
	}

	@PostMapping("/admin/fakeLogin")
	public String switchFakeEmployee(@RequestParam("employeeId") Integer employeeId, HttpSession session) {
		if (employeeId != null && employeeRepo.existsById(employeeId)) {
			session.setAttribute(ACTIVITY_ADMIN_EMPLOYEE_ID, employeeId);
		}
		return "redirect:/activity/admin/home";
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
