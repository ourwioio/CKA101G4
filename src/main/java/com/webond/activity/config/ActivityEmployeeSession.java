package com.webond.activity.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.webond.employee.model.EmployeeVO;
import com.webond.employee.repository.EmployeeRepository;

import jakarta.servlet.http.HttpSession;

@Component
public class ActivityEmployeeSession {

	private static final String EMPLOYEE_VO = "employeeVO";

	@Autowired
	private EmployeeRepository employeeRepo;

	public Integer getLoginEmployeeId(HttpSession session) {
		EmployeeVO loginEmployee = getLoginEmployee(session);
		if (loginEmployee != null) {
			return loginEmployee.getEmployeeId();
		}

		return null;
	}

	public EmployeeVO getLoginEmployee(HttpSession session) {
		Object employeeVO = session.getAttribute(EMPLOYEE_VO);
		if (employeeVO instanceof EmployeeVO) {
			EmployeeVO loginEmployee = (EmployeeVO) employeeVO;
			Integer employeeId = loginEmployee.getEmployeeId();
			if (employeeId != null && employeeRepo.existsById(employeeId)) {
				return loginEmployee;
			}
		}

		return null;
	}

	public boolean isLoginEmployee(HttpSession session) {
		return getLoginEmployeeId(session) != null;
	}

	public String getLoginEmployeeName(HttpSession session) {
		EmployeeVO loginEmployee = getLoginEmployee(session);
		if (loginEmployee != null && hasText(loginEmployee.getEmpName())) {
			return loginEmployee.getEmpName();
		}

		Integer employeeId = getLoginEmployeeId(session);
		EmployeeVO employeeVO = employeeId == null ? null : employeeRepo.findById(employeeId).orElse(null);
		if (employeeVO != null && hasText(employeeVO.getEmpName())) {
			return employeeVO.getEmpName();
		}

		return employeeId == null ? "No employee" : "Employee " + employeeId;
	}

	private boolean hasText(String value) {
		return value != null && !value.trim().isEmpty();
	}
}
