package com.webond.member.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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

		// 📡 🎯 【攔截點】只要有任何錯誤（包括 VO 註解 and 手動 rejectValue），通通退回並攜帶 result 物件
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

	/**
	 * 📊 4. 顯示所有會員大總表（包含已核准、被駁回等所有人）
	 */
	@GetMapping("/back-end/memberList")
	public String memberList(Model model) {
		List<MemberVO> allMembers = memberService.getAllMembers(); 
		model.addAttribute("allMembers", allMembers);
		// 🎯 修正路徑：精準指定到 back-end/member/ 資料夾下
		return "back-end/member/memberList"; 
	}

	// ==========================================
	// 🎯 後台新增：KYC 綜合審核功能面板 (方案 A 模式)
	// ==========================================

	/**
	 * 🖼️ 1. 圖片讀取 API（將資料庫的 byte[] 轉換為網頁圖檔資料流）
	 */
	@GetMapping("/back-end/displayImage")
	@ResponseBody
	public ResponseEntity<byte[]> displayImage(@RequestParam("memberId") Integer memberId, 
	                                           @RequestParam("type") String type) {
		MemberVO memberVO = memberService.getOneMember(memberId); 
		
		if (memberVO != null) {
			byte[] imageBytes = null;
			
			if ("idImage".equals(type)) {
				imageBytes = memberVO.getIdImage(); 
			} else if ("faceImage".equals(type)) {
				imageBytes = memberVO.getFaceImage(); 
			} else if ("memberPic".equals(type)) {
				imageBytes = memberVO.getMemberPic(); 
			}
			
			if (imageBytes != null && imageBytes.length > 0) {
				HttpHeaders headers = new HttpHeaders();
				headers.setContentType(MediaType.IMAGE_JPEG); 
				return new ResponseEntity<>(imageBytes, headers, HttpStatus.OK);
			}
		}
		return new ResponseEntity<>(HttpStatus.NOT_FOUND);
	}

	/**
	 * 🪪 2. 綜合審核面板：一次帶出 待審核、已通過、已駁回 三種清單
	 */
	@GetMapping("/back-end/kycApproval")
	public String kycList(Model model) {
		// 🎯 一次撈取三種狀態名單
		List<MemberVO> pendingList = memberService.getMembersByStatus((byte) 0); // 0 = 待審核
		List<MemberVO> passList = memberService.getMembersByStatus((byte) 1);    // 1 = 已核准
		List<MemberVO> failList = memberService.getMembersByStatus((byte) 2);    // 2 = 已駁回
		
		// 打包裝箱送給前端
		model.addAttribute("pendingList", pendingList);
		model.addAttribute("passList", passList);
		model.addAttribute("failList", failList);
		
		return "back-end/member/kycApproval"; 
	}

	/**
	 * ⚖️ 3. 處理審核結果（通過為 1 / 駁回為 2）
	 */
	@PostMapping("/back-end/reviewKyc")
	public String reviewKyc(@RequestParam("memberId") Integer memberId,
	                        @RequestParam("status") Byte status,
	                        RedirectAttributes redirectAttributes) {
		
		MemberVO memberVO = memberService.getOneMember(memberId);
		if (memberVO != null) {
			// 同步更新帳戶狀態與 KYC 審核狀態
			memberVO.setAccountStatus(status); 
			memberVO.setKycStatus(status); 
			
			memberService.updateMember(memberVO); // 儲存回資料庫
			
			redirectAttributes.addFlashAttribute("successMsg", "會員 [ID: " + memberId + "] 審核操作成功！");
		} else {
			redirectAttributes.addFlashAttribute("errorMsg", "找不到該會員資料！");
		}
		
		// 🎯 修正導向：審核完後一律重新整理導回到綜合面板網址
		return "redirect:/member/back-end/kycApproval"; 
	}
}