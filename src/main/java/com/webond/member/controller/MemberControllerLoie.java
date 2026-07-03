package com.webond.member.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.webond.member.model.MemberVO;
import com.webond.member.service.MemberServiceLoie;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@RequestMapping("/member")
@Controller
public class MemberControllerLoie {

	@Autowired
	private MemberServiceLoie memberService;
	
	@Autowired
	private PasswordEncoder passwordEncoder; 
	
	@GetMapping("/register")
	public String registerPage(Model model) {
		// 🎯 核心關鍵：這行絕對不能漏！Key 必須完全對齊小寫開頭的 "memberVO"
		model.addAttribute("memberVO", new MemberVO());
		return "front-end/member/register";
	}
	
	@GetMapping("/memberSelect_page")
	public String memberSelectPage(HttpSession session, Model model) {
		if (session.getAttribute("memberVO") == null) {
			return "redirect:/member/login";
		}
		return "front-end/member/memberSelect_page";
	}
	
	@GetMapping("/login")
	public String loginPage() {
		return "front-end/member/login"; 
	}

	@PostMapping("/logincontroller")
	public String handleLogin(@RequestParam String email,      
			                  @RequestParam String password,   
			                  HttpSession session,
			                  Model model) {
		
		MemberVO memberVO = memberService.findByEmail(email);

		if (memberVO != null && passwordEncoder.matches(password, memberVO.getPasswordHash())) {
			if (memberVO.getAccountStatus() != null && memberVO.getAccountStatus() == 3) {
				model.addAttribute("email", email);
				model.addAttribute("errorMsgs", "您的帳號已被停權，無法登入！");
				return "front-end/member/login"; 
			}
			session.setAttribute("memberVO", memberVO);
			session.setAttribute("account", memberVO.getEmail());

			String location = (String) session.getAttribute("location");
			return "redirect:" + (location != null ? location : "/member/memberSelect_page");
		} else {
			model.addAttribute("email", email);
			model.addAttribute("errorMsgs", "你的帳號, 密碼無效!");
			return "front-end/member/login"; 
		}
	}

	/**
	 * 📝 處理註冊與 KYC 實名認證請求 (完美對齊 BindingResult 欄位紅字機制)
	 */
	@PostMapping("/doRegister")
	public String doRegister(
			@Valid MemberVO memberVO, // 🎯 關鍵 1：啟動自動驗證
			BindingResult result,     // 🎯 關鍵 2：接收欄位錯誤訊息
			@RequestParam(name = "password") String rawPassword, 
			@RequestParam(name = "memberPicFile") MultipartFile memberPicFile,
			@RequestParam(name = "idImageFile") MultipartFile idImageFile,
			@RequestParam(name = "faceImageFile") MultipartFile faceImageFile,
			Model model) {

		// 📡 偵錯雷達 1
		System.out.println("==== [Debug] 接收到註冊請求！Email: " + memberVO.getEmail() + " ====");

		// 1. 手動檢查特殊欄位：密碼非空檢查
		if (rawPassword == null || rawPassword.trim().isEmpty()) {
			// 🎯 密碼有錯時，直接精準綁定到 "passwordHash" 欄位上！
			result.rejectValue("passwordHash", "error.passwordHash", "密碼請勿空白");
		} else {
			memberVO.setPasswordHash(passwordEncoder.encode(rawPassword)); 
		}

		// 2. 手動檢查特殊欄位：圖片上傳檢查
		try {
			if (memberPicFile != null && !memberPicFile.isEmpty()) {
				memberVO.setMemberPic(memberPicFile.getBytes());
			} else {
				memberVO.setMemberPic(new byte[0]); 
			}
			
			if (idImageFile != null && !idImageFile.isEmpty()) {
				memberVO.setIdImage(idImageFile.getBytes());
			} else {
				// 🎯 身分證圖有錯，直接精準綁定到 "idImage" 欄位上！
				result.rejectValue("idImage", "error.idImage", "請務必上傳身分證件照片");
			}
			
			if (faceImageFile != null && !faceImageFile.isEmpty()) {
				memberVO.setFaceImage(faceImageFile.getBytes());
			} else {
				// 🎯 自拍圖有錯，直接精準綁定到 "faceImage" 欄位上！
				result.rejectValue("faceImage", "error.faceImage", "請務必上傳自拍人臉圖片");
			}
		} catch (IOException e) {
			result.rejectValue("memberPic", "error.memberPic", "圖片處理發生錯誤: " + e.getMessage());
		}

		// 🎯 補填由後端自動生成的預設狀態與時間
		memberVO.setAccountStatus((byte) 0);                               
		memberVO.setKycStatus((byte) 0);                                   
		memberVO.setCreatedAt(java.time.LocalDate.now());                  
		memberVO.setSubmittedAt(java.time.LocalDateTime.now());            

		// 📡 🎯 【攔截點】只要有任何錯誤（包括 VO 註解和手動 rejectValue），通通退回並攜帶 result 物件
		if (result.hasErrors()) {
			System.out.println("==== [Debug] 註冊驗證不通過，已被拒絕！錯誤數量: " + result.getErrorCount() + " ====");
			model.addAttribute("memberVO", memberVO);
			return "front-end/member/register";
		}

		// 📡 偵錯雷達 3：JPA 寫入前夕全面體檢
		System.out.println("====== 🔍 [JPA 寫入前夕：MemberVO 物件內容總體檢] ======");
		System.out.println("欄位 - Email: " + memberVO.getEmail());
		System.out.println("欄位 - Nickname: " + memberVO.getNickname());
		System.out.println("欄位 - RealName: " + memberVO.getRealName());
		System.out.println("欄位 - Phone: " + memberVO.getPhone());
		System.out.println("欄位 - Gender: " + memberVO.getGender());
		System.out.println("欄位 - AccountStatus: " + memberVO.getAccountStatus());
		System.out.println("欄位 - KycStatus: " + memberVO.getKycStatus());
		System.out.println("欄位 - CreatedAt (註冊日期): " + memberVO.getCreatedAt());
		System.out.println("欄位 - SubmittedAt (提交時間): " + memberVO.getSubmittedAt());
		System.out.println("欄位 - 大頭貼(Byte長度): " + (memberVO.getMemberPic() != null ? memberVO.getMemberPic().length : "null"));
		System.out.println("欄位 - 身分證圖(Byte長度): " + (memberVO.getIdImage() != null ? memberVO.getIdImage().length : "null"));
		System.out.println("======================================================");

		try {
			System.out.println("==== [Debug] 格式與圖片檢驗完全通過！準備呼叫 Service 存入資料庫... ====");
			memberService.registerMember(memberVO); 
			System.out.println("==== [Debug] 🎉🎉🎉 資料庫成功寫入一筆新會員！ ====");
			return "redirect:/member/login";
		} catch (Exception e) {
			System.out.println("==== [Debug] ❌ 資料庫拒絕寫入！錯誤細節: " + e.getMessage() + " ====");
			e.printStackTrace(); 
			// 🎯 資料庫噴錯（通常是 Email 重複），直接把錯誤送給 email 欄位顯示紅字
			result.rejectValue("email", "error.database", "註冊失敗：Email 重複註冊或欄位長度超出限制！");
			model.addAttribute("memberVO", memberVO);
			return "front-end/member/register";
		}
	}
}