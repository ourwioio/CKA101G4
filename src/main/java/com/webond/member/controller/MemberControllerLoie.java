package com.webond.member.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
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
import com.webond.member.service.OtpService;
import com.webond.member.service.RedisService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@RequestMapping("/member")
@Controller
public class MemberControllerLoie {
	@Autowired
	private MemberServiceLoie memberService;
	@Autowired
	private RedisService redisService;
	@Autowired
	private PasswordEncoder passwordEncoder;
	@Autowired
	private OtpService otpService;
	
	

	@GetMapping("/register")
	public String registerPage(Model model) {
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

	@GetMapping("/logout")
	public String logout(Model model, HttpSession session) {
		session.invalidate();
		return "front-end/member/login";
	}

	@PostMapping("/logincontroller")
	public String handleLogin(@RequestParam String email, @RequestParam String password, HttpSession session,
			Model model) {
		MemberVO memberVO = memberService.findByEmail(email);
		if (memberVO != null && passwordEncoder.matches(password, memberVO.getPasswordHash())) {
			Byte status = memberVO.getAccountStatus();
			if (status != null) {
				if (status == 0) {
					model.addAttribute("email", email);
					model.addAttribute("errorMsgs", "您的帳號尚在審核中，請等待管理員核准後再登入！");
					return "front-end/member/login";
				} else if (status == 2) {
					model.addAttribute("email", email);
					model.addAttribute("errorMsgs", "此帳號已被註銷或實名認證未通過！");
					return "front-end/member/login";
				} else if (status == 3) {
					model.addAttribute("email", email);
					model.addAttribute("errorMsgs", "您的帳號因違規已被系統停權，無法登入！");
					return "front-end/member/login";
				}
			}
			session.setAttribute("memberVO", memberVO);
			session.setAttribute("account", memberVO.getEmail());
			String location = (String) session.getAttribute("location");
			return "redirect:" + (location != null ? location : "/");
		} else {
			model.addAttribute("email", email);
			model.addAttribute("errorMsgs", "您的帳號或密碼無效！");
			return "front-end/member/login";
		}
	}

	// =========================================================================
	// 🔑 忘記密碼模組 (Redis 串接)
	// =========================================================================
	// 1. 前往忘記密碼頁面
	@GetMapping("/forgotPassword")
	public String forgotPasswordPage() {
		return "front-end/member/forgotPassword";
	}

	// 2. AJAX 發送忘記密碼驗證碼至 Redis
	@PostMapping("/send-forgot-otp")
	@ResponseBody
	public Map<String, Object> sendForgotOtp(@RequestParam String email) {
		Map<String, Object> result = new HashMap<>();
		if (email == null || email.trim().isEmpty()) {
			result.put("success", false);
			result.put("message", "請輸入 Email！");
			return result;
		}
		// 檢查 60 秒冷卻
		if (redisService.isCoolingDown(email)) {
			result.put("success", false);
			result.put("message", "請求過於頻繁，請稍後再試！");
			return result;
		}
		// 檢查 Email 是否有註冊過
		MemberVO memberVO = memberService.findByEmail(email.trim());
		if (memberVO == null) {
			result.put("success", false);
			result.put("message", "該 Email 尚未註冊會員！");
			return result;
		}
		// 🔒 帳號狀態檢查：只有「正常使用中(1)」的帳號才能重設密碼
		Byte status = memberVO.getAccountStatus();
		if (status != null && status != 1) {
			result.put("success", false);
			result.put("message", buildAccountStatusMessage(status));
			return result;
		}
		// 產生 6 位數隨機數字驗證碼
		String otpCode = String.format("%06d", new Random().nextInt(900000) + 100000);
		// 存入 Redis (5 分鐘有效，60 秒冷卻)
		redisService.saveForgotOtp(email, otpCode);
		// TODO: 呼叫 MailService 寄出信件
		System.out.println("【重設密碼測試】發送驗證碼至 " + email + " 驗證碼為：" + otpCode);
		result.put("success", true);
		result.put("message", "驗證碼已寄至您的信箱，請於 5 分鐘內輸入！");
		return result;
	}

	// 3. 送出重設密碼表單
	@PostMapping("/resetPassword")
	public String resetPassword(@RequestParam String email, @RequestParam String otpCode,
			@RequestParam String newPassword, RedirectAttributes redirectAttributes, Model model) {
		boolean hasError = false;
		// 1. 檢查 Email 是否存在
		MemberVO memberVO = memberService.findByEmail(email);
		if (memberVO == null) {
			model.addAttribute("emailError", "找不到該 Email 對應的帳號！");
			hasError = true;
		}
		// 🔒 2. 帳號狀態檢查：只有「正常使用中(1)」的帳號才能重設密碼
		if (memberVO != null) {
			Byte status = memberVO.getAccountStatus();
			if (status != null && status != 1) {
				model.addAttribute("emailError", buildAccountStatusMessage(status));
				hasError = true;
			}
		}
		// 3. 檢查 Redis 內的驗證碼
		String realOtp = redisService.getForgotOtp(email);
		if (realOtp == null) {
			model.addAttribute("otpError", "驗證碼已過期或未發送，請重新取得！");
			hasError = true;
		} else if (!realOtp.equals(otpCode.trim())) {
			model.addAttribute("otpError", "驗證碼不正確！");
			hasError = true;
		}
		// 4. 檢查新密碼
		if (newPassword == null || newPassword.trim().isEmpty()) {
			model.addAttribute("passwordError", "新密碼請勿空白！");
			hasError = true;
		}
		// 若驗證失敗，返回重設頁面並帶回原本輸入的 Email
		if (hasError) {
			model.addAttribute("email", email);
			return "front-end/member/forgotPassword";
		}
		// 5. 更新 DB 密碼
		memberVO.setPasswordHash(passwordEncoder.encode(newPassword));
		memberService.updateMember(memberVO);
		// 6. 密碼重設成功，清理 Redis 驗證碼
		redisService.deleteForgotOtp(email);
		redirectAttributes.addFlashAttribute("successMsg", "密碼重設成功，請使用新密碼登入！");
		return "redirect:/member/login";
	}
	// 依帳號狀態產生對應的封鎖訊息（登入、忘記密碼共用邏輯保持一致）
	private String buildAccountStatusMessage(Byte status) {
		if (status == 0) {
			return "您的帳號尚在審核中，請等待管理員核准後再使用忘記密碼功能！";
		} else if (status == 2) {
			return "此帳號已被註銷或實名認證未通過，如有疑問請聯繫客服！";
		} else if (status == 3) {
			return "您的帳號因違規已被系統停權，暫無法重設密碼！";
		}
		return "此帳號目前無法使用忘記密碼功能！";
	}
	

	// =========================================================================
	// 📝 API 2：註冊表單送出（對齊前端個別欄位下方顯示錯誤提示）
	// =========================================================================
	@PostMapping("/doRegister")
	public String doRegister(@Valid MemberVO memberVO, BindingResult result,
			@RequestParam(name = "otpCode", required = false) String inputOtpCode,
			@RequestParam(name = "password", required = false) String rawPassword,
			@RequestParam(name = "memberPicFile", required = false) MultipartFile memberPicFile,
			@RequestParam(name = "idFrontImageFile", required = false) MultipartFile idFrontImageFile,
			@RequestParam(name = "idBackImageFile", required = false) MultipartFile idBackImageFile,
			@RequestParam(name = "faceImageFile", required = false) MultipartFile faceImageFile,
			@RequestParam(name = "tempMemberPic", required = false) String tempMemberPic,
			@RequestParam(name = "tempIdFrontImage", required = false) String tempIdFrontImage,
			@RequestParam(name = "tempIdBackImage", required = false) String tempIdBackImage,
			@RequestParam(name = "tempFaceImage", required = false) String tempFaceImage, HttpSession session,
			Model model) {
		if (rawPassword == null || rawPassword.trim().isEmpty()) {
			result.rejectValue("passwordHash", "error.password", "密碼請勿空白");
		} else {
			memberVO.setPasswordHash(passwordEncoder.encode(rawPassword));
		}
		byte[] memberPicBytes = processImageBytes1(memberPicFile, tempMemberPic);
		byte[] faceImageBytes = processImageBytes1(faceImageFile, tempFaceImage);
		byte[] idFrontBytes = processImageBytes1(idFrontImageFile, tempIdFrontImage);
		byte[] idBackBytes = processImageBytes1(idBackImageFile, tempIdBackImage);
		boolean hasImageError = false;
		if (idFrontBytes == null || idFrontBytes.length == 0) {
			model.addAttribute("idFrontImageError", "請上傳身分證正面！");
			hasImageError = true;
		}
		if (faceImageBytes == null || faceImageBytes.length == 0) {
			result.rejectValue("faceImage", "error.faceImage", "請務必拍攝人臉照片");
			hasImageError = true;
		}
		String combinedBase64 = "{\"front\":\"" + encodeToBase641(idFrontBytes) + "\",\"back\":\""
				+ encodeToBase641(idBackBytes) + "\"}";
		memberVO.setMemberPic(memberPicBytes != null ? memberPicBytes : new byte[0]);
		memberVO.setIdImage(combinedBase64.getBytes());
		memberVO.setFaceImage(faceImageBytes);

		// 🔒 後端把關：確認此 Email 已完成 OTP 驗證（防止繞過前端直接送出表單註冊）
		String cleanEmail = memberVO.getEmail() != null ? memberVO.getEmail().trim() : null;
		if (cleanEmail == null || !otpService.isVerified(cleanEmail)) {
			model.addAttribute("otpError", "請先完成 Email 驗證碼驗證後再送出註冊！");
			hasImageError = true; // 借用既有的錯誤旗標一併導回表單並保留已上傳的圖片
		}

		if (result.hasErrors() || hasImageError) {
			model.addAttribute("memberVO", memberVO);
			model.addAttribute("tempMemberPic", encodeToBase641(memberPicBytes));
			model.addAttribute("tempIdFrontImage", encodeToBase641(idFrontBytes));
			model.addAttribute("tempIdBackImage", encodeToBase641(idBackBytes));
			model.addAttribute("tempFaceImage", encodeToBase641(faceImageBytes));
			return "front-end/member/register";
		}
		try {
			memberService.registerMember(memberVO);
			otpService.clearVerified(cleanEmail); // 註冊成功，清除驗證標記避免殘留
			session.setAttribute("memberVO", memberVO);
			return "redirect:/";
		} catch (Exception e) {
			result.rejectValue("email", "error.database", "註冊失敗，請稍後再試！");
			return "front-end/member/register";
		}
	}

	private byte[] processImageBytes1(MultipartFile file, String tempBase64) {
		if (file != null && !file.isEmpty()) {
			try {
				return file.getBytes();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if (tempBase64 != null && !tempBase64.trim().isEmpty()) {
			try {
				String cleanBase64 = tempBase64.contains(",") ? tempBase64.split(",")[1] : tempBase64;
				return Base64.getDecoder().decode(cleanBase64.replaceAll("\\s", ""));
			} catch (IllegalArgumentException e) {
				return null;
			}
		}
		return null;
	}

	private String encodeToBase641(byte[] imageBytes) {
		return (imageBytes != null && imageBytes.length > 0) ? Base64.getEncoder().encodeToString(imageBytes) : "";
	}

//	@GetMapping("/back-end/memberList")
//	public String memberList(@RequestParam(name = "keyword", required = false) String keyword, Model model) {
//		List<MemberVO> allMembers;
//		if (keyword != null && !keyword.trim().isEmpty()) {
//			allMembers = memberService.searchMembers(keyword.trim());
//			model.addAttribute("keyword", keyword.trim());
//		} else {
//			allMembers = memberService.getAllMembers();
//		}
//		List<MemberVO> sortedMembers = new ArrayList<>(allMembers);
//		sortedMembers.sort((m1, m2) -> {
//			int p1 = m1.getReportPoints() != null ? m1.getReportPoints() : 0;
//			int p2 = m2.getReportPoints() != null ? m2.getReportPoints() : 0;
//			return Integer.compare(p2, p1);
//		});
//		model.addAttribute("allMembers", sortedMembers);
//		return "back-end/member/memberList";
//	}
//
//	@PostMapping("/back-end/toggleStatus")
//	public String toggleStatus(@RequestParam("memberId") Integer memberId, @RequestParam("newStatus") Byte newStatus,
//			RedirectAttributes redirectAttributes) {
//		MemberVO memberVO = memberService.getOneMember(memberId);
//		if (memberVO != null) {
//			memberVO.setAccountStatus(newStatus);
//			if (newStatus == 1) {
//				memberVO.setKycStatus((byte) 1);
//				memberVO.setReportPoints(0);
//			}
//			if (newStatus == 2) {
//				memberVO.setKycStatus((byte) 2);
//				memberVO.setAccountStatus((byte) 2);
//			}
//			memberService.updateMember(memberVO);
//			String msg = "會員 [ID: " + memberId + "] 狀態已成功更新！";
//			if (newStatus == 3)
//				msg = "該會員已被成功停權！";
//			if (newStatus == 1)
//				msg = "該會員帳號已核准正常使用（檢舉點數已重置）！";
//			if (newStatus == 2)
//				msg = "該會員已被標記為實名認證失敗/註銷！";
//			redirectAttributes.addFlashAttribute("successMsg", msg);
//		} else {
//			redirectAttributes.addFlashAttribute("errorMsg", "找不到該會員資料！");
//		}
//		return "redirect:/member/back-end/memberList";
//	}
//
//	@GetMapping("/back-end/displayImage")
//	@ResponseBody
//	public ResponseEntity<byte[]> displayImage(@RequestParam("memberId") Integer memberId,
//			@RequestParam("type") String type) {
//		
//		MemberVO memberVO = memberService.getOneMember(memberId);
//		// 基礎防禦：會員不存在或圖片欄位為空則回傳 404
//		if (memberVO == null || memberVO.getIdImage() == null) {
//			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
//		}
//
//		try {
//			// 1. 處理身分證正反面 (JSON 格式儲存)
//			if ("idFront".equals(type) || "idBack".equals(type)) {
//				String jsonStr = new String(memberVO.getIdImage(), "UTF-8");
//				// 根據 type 對應 JSON 中的 Key: front 或 back
//				String key = "idFront".equals(type) ? "front" : "back";
//
//				// 手動解析 JSON 字串，避免額外的 Library 依賴
//				String searchKey = "\"" + key + "\":\"";
//				int start = jsonStr.indexOf(searchKey);
//				if (start == -1) return new ResponseEntity<>(HttpStatus.NOT_FOUND);
//
//				start += searchKey.length();
//				int end = jsonStr.indexOf("\"", start);
//				String base64 = jsonStr.substring(start, end);
//
//				// 解碼並清除 Base64 可能存在的換行符號
//				byte[] imageBytes = Base64.getDecoder().decode(base64.replaceAll("\\s", ""));
//
//				HttpHeaders headers = new HttpHeaders();
//				headers.setContentType(MediaType.IMAGE_JPEG);
//				return new ResponseEntity<>(imageBytes, headers, HttpStatus.OK);
//			} 
//			// 2. 處理人臉照片 (直接儲存的 BLOB)
//			else if ("faceImage".equals(type)) {
//				if (memberVO.getFaceImage() == null) return new ResponseEntity<>(HttpStatus.NOT_FOUND);
//				return ResponseEntity.ok()
//						.contentType(MediaType.IMAGE_JPEG)
//						.body(memberVO.getFaceImage());
//			} 
//			// 3. 處理大頭照
//			else if ("memberPic".equals(type)) {
//				if (memberVO.getMemberPic() == null) return new ResponseEntity<>(HttpStatus.NOT_FOUND);
//				return ResponseEntity.ok()
//						.contentType(MediaType.IMAGE_JPEG)
//						.body(memberVO.getMemberPic());
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		return new ResponseEntity<>(HttpStatus.NOT_FOUND);
//	}
	// -------------------------------------------------------------------------
	// 🛠️ 輔助方法（Helper Methods）
	// -------------------------------------------------------------------------
	private byte[] processImageBytes(MultipartFile file, String tempBase64) {
		if (file != null && !file.isEmpty()) {
			try {
				return file.getBytes();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if (tempBase64 != null && !tempBase64.trim().isEmpty()) {
			try {
				// 🟢 自動去除 Base64 前綴，防止字串解碼失敗
				String cleanBase64 = tempBase64;
				if (cleanBase64.contains(",")) {
					cleanBase64 = cleanBase64.split(",")[1];
				}
				return Base64.getDecoder().decode(cleanBase64.trim());
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	private String encodeToBase64(byte[] imageBytes) {
		if (imageBytes != null && imageBytes.length > 0) {
			return Base64.getEncoder().encodeToString(imageBytes);
		}
		return "";
		
	}
}