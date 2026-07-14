package com.webond.activity.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.webond.employee.model.EmployeeVO;
import com.webond.employee.repository.EmployeeRepository;

import jakarta.servlet.http.HttpSession;

@Component
public class ActivityEmployeeSession {

	public static final String ACTIVITY_ADMIN_EMPLOYEE_ID = "activityAdminEmployeeId";

	private static final String LOGIN_EMPLOYEE = "loginEmp";
	private static final String LOGIN_EMPLOYEE_ID = "loginEmployeeId";

	@Autowired
	private EmployeeRepository employeeRepo;

	public Integer getLoginEmployeeId(HttpSession session) {
		EmployeeVO loginEmployee = getLoginEmployee(session);
		if (loginEmployee != null) {
			return loginEmployee.getEmployeeId();
		}

		Object loginEmployeeId = session.getAttribute(LOGIN_EMPLOYEE_ID);
		if (loginEmployeeId instanceof Integer && employeeRepo.existsById((Integer) loginEmployeeId)) {
			return (Integer) loginEmployeeId;
		}

		Object activityEmployeeId = session.getAttribute(ACTIVITY_ADMIN_EMPLOYEE_ID);
		if (activityEmployeeId instanceof Integer && employeeRepo.existsById((Integer) activityEmployeeId)) {
			return (Integer) activityEmployeeId;
		}

		return null;
	}

	public EmployeeVO getLoginEmployee(HttpSession session) {
		Object loginEmployee = session.getAttribute(LOGIN_EMPLOYEE);
		if (loginEmployee instanceof EmployeeVO) {
			EmployeeVO employeeVO = (EmployeeVO) loginEmployee;
			Integer employeeId = employeeVO.getEmployeeId();
			if (employeeId != null && employeeRepo.existsById(employeeId)) {
				return employeeVO;
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

	public void switchActivityTestEmployee(HttpSession session, Integer employeeId) {
		if (employeeId != null && employeeRepo.existsById(employeeId)) {
			session.setAttribute(ACTIVITY_ADMIN_EMPLOYEE_ID, employeeId);
		} else {
			session.removeAttribute(ACTIVITY_ADMIN_EMPLOYEE_ID);
		}
	}

	private boolean hasText(String value) {
		return value != null && !value.trim().isEmpty();
	}
}
