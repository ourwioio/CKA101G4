package com.webond.securityconfig.admin;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.webond.employee.model.EmployeeVO;
import com.webond.employee.repository.EmployeeRepository;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;


@Component
public class AdminLoginSuccessHandler implements AuthenticationSuccessHandler {

	private final EmployeeRepository empRepository;
	
	public AdminLoginSuccessHandler(EmployeeRepository empRepository) {
		this.empRepository = empRepository;
	}

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
			Authentication authentication) throws IOException, ServletException {
		
		String empAccount = authentication.getName();
		Optional<EmployeeVO> empOptional = empRepository.findByEmpAccount(empAccount);
		
		EmployeeVO emp = empOptional.orElse(null);
		
		
		
		if(emp !=null) {
			
			LocalDateTime now = LocalDateTime.now();
			emp.setLastLoginAt(now);
			
			empRepository.updateLastLoginAt(emp.getEmployeeId(), now);
			
			HttpSession session = request.getSession();
	        session.setAttribute("loginEmp", emp);
	        
			if(Integer.valueOf(0).equals(emp.getEmpStatus())) {
				response.sendRedirect(request.getContextPath()+"/admin/updatePassword");
			}else{
				response.sendRedirect(request.getContextPath() + "/admin/adminPage");
			}
	        
		}else {
			response.sendRedirect(request.getContextPath() + "/login?error");
		}
	}
	
	
	
	
}
