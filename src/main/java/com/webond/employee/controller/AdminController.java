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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.webond.employee.dto.EmpPasswordDTO;
import com.webond.employee.model.EmpService;
import com.webond.employee.model.EmployeeVO;
import com.webond.securityconfig.admin.AdminForgotPasswordService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/admin")
public class AdminController {

	@Autowired
	private PasswordEncoder passwordEncoder;
	
	@Autowired
	private AdminForgotPasswordService adminForgotPasswordSvc;

	private final EmpService empSvc;

	public AdminController(EmpService empSvc) {
		this.empSvc = empSvc;
	}

	// 登入頁面
	@GetMapping("/login")
	public String loginPage(HttpSession session, Model model) {

		String loginErrorMessage = (String) session.getAttribute("loginErrorMessage");

		if (loginErrorMessage != null) {
			model.addAttribute("loginErrorMessage", loginErrorMessage);

			session.removeAttribute("loginErrorMessage");
		}
		return "back-end/employee/login";
	}

	
	@GetMapping("/adminPage")
	public String adminPage() {
		return "back-end/employee/index";
	}

	@GetMapping("/updatePassword")
	public String updatePassword(Model model) {
		model.addAttribute("empPasswordDTO", new EmpPasswordDTO());

		return "back-end/employee/updatePassword";
	}

// === 上傳的修改密碼資料 ===//
	@PostMapping("/upPassword")
	public String upPassword(@Valid @ModelAttribute("empPasswordDTO") EmpPasswordDTO empPassword, BindingResult result,
			@SessionAttribute("employeeVO") EmployeeVO loginEmp, Model model) {

		if (result.hasErrors()) {
			return "back-end/employee/updatePassword";
		}

		boolean isOldPasswordCorrect = empSvc.checkOldPassword(loginEmp.getEmpAccount(), empPassword);
		if (!isOldPasswordCorrect) {
			model.addAttribute("errorMessage", "目前的初始密碼輸入錯誤");
			return "back-end/employee/updatePassword";
		}

		if (empPassword.getNewPassword().equals(empPassword.getCurrentPassword())) {
			model.addAttribute("errorMessage", "新密碼不能與舊密碼相同");
			return "back-end/employee/updatePassword";
		}

		if (!empPassword.getNewPassword().equals(empPassword.getConfirmPassword())) {
			model.addAttribute("errorMessage", "兩次輸入的新密碼不一致");
			return "back-end/employee/updatePassword";
		}

		empSvc.updatePassword(loginEmp.getEmpAccount(), empPassword);

		model.addAttribute("successMessage", "密碼修改成功！");

		return "back-end/employee/index";
	}
	
	// 忘記密碼畫面	
	@GetMapping("/forgotPassword")
	public String forgotPassword(Model model) {

		return "back-end/employee/forgotPassword";
	}

	
	// 接收 Email 申請
	@PostMapping("/forgotPassword")
	public String handleForgotPassword(
	        @RequestParam("username") String username, 
	        HttpServletRequest request, 
	        RedirectAttributes redirectAttributes) { // 💡 1. 引入 RedirectAttributes
	    
	    adminForgotPasswordSvc.processForgotPassword(username, request);
	    
	    redirectAttributes.addFlashAttribute("message", "若此帳號存在於系統中，重設連結已發送至該信箱。");
	    
	    return "redirect:/admin/forgotPassword"; 
	}

    // 顯示輸入新密碼的頁面
    @GetMapping("/resetPassword")
    public String showResetPasswordForm(@RequestParam("token") String token, Model model) {
        if (!adminForgotPasswordSvc.validatePasswordResetToken(token)) {
            model.addAttribute("errorMessage", "此連結已失效或已過期，請重新申請。");
            return "back-end/employee/forgotPassword"; 
        }
        
        EmpPasswordDTO empPasswordDTO = new EmpPasswordDTO();
        empPasswordDTO.setCurrentPassword(token); 
        
        model.addAttribute("empPasswordDTO", empPasswordDTO);
        model.addAttribute("token", token);
        return "back-end/employee/resetPassword"; 
    }

    //  接收新密碼提交
    @PostMapping("/resetPassword")
    public String handleResetPassword(
            @Valid @ModelAttribute("empPasswordDTO") EmpPasswordDTO empPassword, // 🌟 啟動你寫好的 GroupSequence 驗證
            BindingResult result,
            org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes,
            Model model) {
        
        if (result.hasErrors()) {
            return "back-end/employee/resetPassword";
        }

        if (!empPassword.getNewPassword().equals(empPassword.getConfirmPassword())) {
            model.addAttribute("errorMessage", "兩次輸入的新密碼不一致");
            return "back-end/employee/resetPassword";
        }

        String token = empPassword.getCurrentPassword();

        if (!adminForgotPasswordSvc.validatePasswordResetToken(token)) {
            redirectAttributes.addFlashAttribute("errorMessage", "安全驗證逾時，請重新申請。");
            return "redirect:/admin/forgotPassword";
        }

        boolean isSuccess = adminForgotPasswordSvc.updatePassword(token, empPassword.getNewPassword());
        
        if (isSuccess) {
            return "redirect:/admin/login?resetSuccess=true";
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "密碼重設失敗，請重新申請。");
            return "redirect:/admin/forgotPassword";
        }
    }
}
