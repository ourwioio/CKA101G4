package com.webond.employee.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttribute;

import com.webond.employee.dto.EmpPasswordDTO;
import com.webond.employee.model.EmpService;
import com.webond.employee.model.EmployeeVO;
import com.webond.member.model.MemberVO;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/admin")
public class AdminController {
	
	@Autowired
	private PasswordEncoder passwordEncoder;
	
	private final EmpService empSvc;
	
	public AdminController(EmpService empSvc) {
		this.empSvc = empSvc;
	}
	
	
	
	//登入頁面
	@GetMapping("/login")
	public String loginPage() {
		return"back-end/employee/login";
	}
	
	@GetMapping("/adminPage")
	public String adminPage() {
		return"back-end/employee/index";
	}
	
	@GetMapping("/updatePassword")
	public String updatePassword(Model model) {
		model.addAttribute("empPasswordDTO", new EmpPasswordDTO()); 
		
		return"back-end/employee/updatePassword";
	}

	
// === 上傳的修改密碼資料 ===//
	@PostMapping("/upPassword")
	public String upPassword(
			@Valid @ModelAttribute("empPasswordDTO") EmpPasswordDTO empPassword,
			BindingResult result,
			@SessionAttribute("employeeVO") EmployeeVO loginEmp,
			Model model) {
		
		if(result.hasErrors()) {
			return "back-end/employee/updatePassword";
		}
		
		boolean isOldPasswordCorrect = empSvc.checkOldPassword(loginEmp.getEmpAccount(), empPassword);
	    if (!isOldPasswordCorrect) {
	        model.addAttribute("errorMessage", "目前的初始密碼輸入錯誤");
	        return "back-end/employee/updatePassword";
	    }	
	    
	    if(empPassword.getNewPassword().equals(empPassword.getCurrentPassword())) {
	    	model.addAttribute("errorMessage", "新密碼不能與舊密碼相同");
	    	return "back-end/employee/updatePassword";
	    }
	    
		if(!empPassword.getNewPassword().equals(empPassword.getConfirmPassword())) {
			model.addAttribute("errorMessage", "兩次輸入的新密碼不一致");
			return "back-end/employee/updatePassword";
		}
		

	    
	    empSvc.updatePassword(loginEmp.getEmpAccount(), empPassword);
	    
	    model.addAttribute("successMessage", "密碼修改成功！");
		
		
		return"back-end/employee/index";
	}
	
	

}



