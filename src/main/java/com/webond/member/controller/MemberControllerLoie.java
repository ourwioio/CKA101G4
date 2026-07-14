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
	public String resetPassword(
			@RequestParam String email,
			@RequestParam String otpCode,
			@RequestParam String newPassword,
			RedirectAttributes redirectAttributes,
			Model model) {

		boolean hasError = false;

		// 1. 檢查 Email 是否存在
		MemberVO memberVO = memberService.findByEmail(email);
		if (memberVO == null) {
			model.addAttribute("emailError", "找不到該 Email 對應的帳號！");
			hasError = true;
		}

		// 2. 檢查 Redis 內的驗證碼
		String realOtp = redisService.getForgotOtp(email);
		if (realOtp == null) {
			model.addAttribute("otpError", "驗證碼已過期或未發送，請重新取得！");
			hasError = true;
		} else if (!realOtp.equals(otpCode.trim())) {
			model.addAttribute("otpError", "驗證碼不正確！");
			hasError = true;
		}

		// 3. 檢查新密碼
		if (newPassword == null || newPassword.trim().isEmpty()) {
			model.addAttribute("passwordError", "新密碼請勿空白！");
			hasError = true;
		}

		// 若驗證失敗，返回重設頁面並帶回原本輸入的 Email
		if (hasError) {
			model.addAttribute("email", email);
			return "front-end/member/forgotPassword";
		}

		// 4. 更新 DB 密碼
		memberVO.setPasswordHash(passwordEncoder.encode(newPassword));
		memberService.updateMember(memberVO);

		// 5. 密碼重設成功，清理 Redis 驗證碼
		redisService.deleteForgotOtp(email);

		redirectAttributes.addFlashAttribute("successMsg", "密碼重設成功，請使用新密碼登入！");
		return "redirect:/member/login";
	}

	// =========================================================================
	// 📝 API 2：註冊表單送出（對齊前端個別欄位下方顯示錯誤提示）
	// =========================================================================
	@PostMapping("/doRegister")
	public String doRegister(@Valid MemberVO memberVO, BindingResult result,
			@RequestParam(name = "otpCode", required = false) String inputOtpCode,
			@RequestParam(name = "password", required = false) String rawPassword,
			@RequestParam(name = "memberPicFile", required = false) MultipartFile memberPicFile,
			@RequestParam(name = "idFrontImageFile", required = false) MultipartFile idFrontImageFile, // 🟢 正面
			@RequestParam(name = "idBackImageFile", required = false) MultipartFile idBackImageFile,   // 🟢 反面
			@RequestParam(name = "faceImageFile", required = false) MultipartFile faceImageFile,
			@RequestParam(name = "tempMemberPic", required = false) String tempMemberPic,
			@RequestParam(name = "tempIdFrontImage", required = false) String tempIdFrontImage,         // 🟢 正面暫存
			@RequestParam(name = "tempIdBackImage", required = false) String tempIdBackImage,           // 🟢 反面暫存
			@RequestParam(name = "tempIdImage", required = false) String tempIdImage,                   // 🟢 相容舊隱藏欄位
			@RequestParam(name = "tempFaceImage", required = false) String tempFaceImage, 
			HttpSession session, Model model) {

		// 1. 處理密碼驗證（掛至 passwordHash 欄位下方）
		if (rawPassword == null || rawPassword.trim().isEmpty()) {
			result.rejectValue("passwordHash", "error.passwordHash", "密碼請勿空白");
		} else {
			memberVO.setPasswordHash(passwordEncoder.encode(rawPassword));
		}

		// 2. 驗證碼檢查（傳至 otpError Key 供獨立顯示）
		if (inputOtpCode == null || inputOtpCode.trim().isEmpty()) {
			model.addAttribute("otpError", "請輸入信箱驗證碼！");
		}

		// 3. 處理圖片（頭像與人臉）
		byte[] memberPicBytes = processImageBytes(memberPicFile, tempMemberPic);
		byte[] faceImageBytes = processImageBytes(faceImageFile, tempFaceImage);

		// 🟢 處理身分證（正面與反面）
		byte[] idFrontBytes = processImageBytes(idFrontImageFile, tempIdFrontImage);
		byte[] idBackBytes = processImageBytes(idBackImageFile, tempIdBackImage);

		// 💡 相容性處置：若前端只傳單一 tempIdImage，備用為正面檔
		if ((idFrontBytes == null || idFrontBytes.length == 0) && tempIdImage != null && !tempIdImage.trim().isEmpty()) {
			idFrontBytes = processImageBytes(null, tempIdImage);
		}

		// 組合身分證 bytes 供 Entity 儲存
		byte[] finalIdImageBytes = idFrontBytes;
		if (idFrontBytes != null && idFrontBytes.length > 0 && idBackBytes != null && idBackBytes.length > 0) {
			String combinedBase64 = "{\"front\":\"" + encodeToBase64(idFrontBytes) + "\",\"back\":\"" + encodeToBase64(idBackBytes) + "\"}";
			finalIdImageBytes = combinedBase64.getBytes();
		}

		memberVO.setMemberPic(memberPicBytes != null ? memberPicBytes : new byte[0]);
		memberVO.setIdImage(finalIdImageBytes);
		memberVO.setFaceImage(faceImageBytes);

		// 🟢 嚴格檢查：將身分證正反面與人臉照片錯誤精確分派給對應 Key/欄位
		boolean hasImageError = false;
		if (idFrontBytes == null || idFrontBytes.length == 0) {
			model.addAttribute("idFrontImageError", "請務必上傳身分證【正面】照片！");
			hasImageError = true;
		}
		if (idBackBytes == null || idBackBytes.length == 0) {
			model.addAttribute("idBackImageError", "請務必上傳身分證【反面】照片！");
			hasImageError = true;
		}
		if (faceImageBytes == null || faceImageBytes.length == 0) {
			result.rejectValue("faceImage", "error.faceImage", "請務必上傳自拍人臉圖片");
		}

		// 預設帳號與實名狀態
		memberVO.setAccountStatus((byte) 0);
		memberVO.setKycStatus((byte) 0);
		memberVO.setCreatedAt(java.time.LocalDate.now());
		memberVO.setSubmittedAt(java.time.LocalDateTime.now());

		// 🟢 4. 驗證失敗處理：將正面、反面、人臉及頭像暫存分別存入 Model 返回前端渲染
		if (result.hasErrors() || hasImageError || model.containsAttribute("otpError")) {
			model.addAttribute("memberVO", memberVO);
			model.addAttribute("rawPassword", rawPassword);
			model.addAttribute("inputOtpCode", inputOtpCode);
			model.addAttribute("tempMemberPic", encodeToBase64(memberPicBytes));
			model.addAttribute("tempIdFrontImage", encodeToBase64(idFrontBytes)); // 正面暫存
			model.addAttribute("tempIdBackImage", encodeToBase64(idBackBytes));   // 反面暫存
			model.addAttribute("tempIdImage", encodeToBase64(finalIdImageBytes));
			model.addAttribute("tempFaceImage", encodeToBase64(faceImageBytes));
			return "front-end/member/register";
		}

		// 5. 送出資料庫寫入，並清理 Redis 驗證碼
		try {
			memberService.registerMember(memberVO);

			// 註冊成功，清理 Redis 註冊驗證碼
			redisService.deleteRegisterOtp(memberVO.getEmail());

			// 🟢【登入與跳轉】自動將註冊帳號寫入 Session 保持登入狀態
			session.setAttribute("memberVO", memberVO);
			session.setAttribute("account", memberVO.getEmail());

			return "redirect:/"; 

		} catch (Exception e) {
			e.printStackTrace();
			result.rejectValue("email", "error.database", "註冊失敗：Email 重複註冊或資料格式不正確！");
			model.addAttribute("memberVO", memberVO);
			model.addAttribute("rawPassword", rawPassword);
			model.addAttribute("inputOtpCode", inputOtpCode);
			model.addAttribute("tempMemberPic", encodeToBase64(memberPicBytes));
			model.addAttribute("tempIdFrontImage", encodeToBase64(idFrontBytes));
			model.addAttribute("tempIdBackImage", encodeToBase64(idBackBytes));
			model.addAttribute("tempIdImage", encodeToBase64(finalIdImageBytes));
			model.addAttribute("tempFaceImage", encodeToBase64(faceImageBytes));
			return "front-end/member/register";
		}
	}

	@GetMapping("/back-end/memberList")
	public String memberList(@RequestParam(name = "keyword", required = false) String keyword, Model model) {
		List<MemberVO> allMembers;
		if (keyword != null && !keyword.trim().isEmpty()) {
			allMembers = memberService.searchMembers(keyword.trim());
			model.addAttribute("keyword", keyword.trim());
		} else {
			allMembers = memberService.getAllMembers();
		}

		List<MemberVO> sortedMembers = new ArrayList<>(allMembers);
		sortedMembers.sort((m1, m2) -> {
			int p1 = m1.getReportPoints() != null ? m1.getReportPoints() : 0;
			int p2 = m2.getReportPoints() != null ? m2.getReportPoints() : 0;
			return Integer.compare(p2, p1);
		});

		model.addAttribute("allMembers", sortedMembers);
		return "back-end/member/memberList";
	}

	@PostMapping("/back-end/toggleStatus")
	public String toggleStatus(@RequestParam("memberId") Integer memberId, @RequestParam("newStatus") Byte newStatus,
			RedirectAttributes redirectAttributes) {

		MemberVO memberVO = memberService.getOneMember(memberId);
		if (memberVO != null) {
			memberVO.setAccountStatus(newStatus);

			if (newStatus == 1) {
				memberVO.setKycStatus((byte) 1);
				memberVO.setReportPoints(0);
			}
			if (newStatus == 2) {
				memberVO.setKycStatus((byte) 2);
				memberVO.setAccountStatus((byte) 2);
			}

			memberService.updateMember(memberVO);

			String msg = "會員 [ID: " + memberId + "] 狀態已成功更新！";
			if (newStatus == 3)
				msg = "該會員已被成功停權！";
			if (newStatus == 1)
				msg = "該會員帳號已核准正常使用（檢舉點數已重置）！";
			if (newStatus == 2)
				msg = "該會員已被標記為實名認證失敗/註銷！";

			redirectAttributes.addFlashAttribute("successMsg", msg);
		} else {
			redirectAttributes.addFlashAttribute("errorMsg", "找不到該會員資料！");
		}

		return "redirect:/member/back-end/memberList";
	}

	@GetMapping("/back-end/displayImage")
	@ResponseBody
	public ResponseEntity<byte[]> displayImage(@RequestParam("memberId") Integer memberId,
			@RequestParam("type") String type) {
		MemberVO memberVO = memberService.getOneMember(memberId);
		if (memberVO != null) {
			byte[] imageBytes = null;
			if ("idImage".equals(type))
				imageBytes = memberVO.getIdImage();
			else if ("faceImage".equals(type))
				imageBytes = memberVO.getFaceImage();
			else if ("memberPic".equals(type))
				imageBytes = memberVO.getMemberPic();

			if (imageBytes != null && imageBytes.length > 0) {
				HttpHeaders headers = new HttpHeaders();
				headers.setContentType(MediaType.IMAGE_JPEG);
				return new ResponseEntity<>(imageBytes, headers, HttpStatus.OK);
			}
		}
		return new ResponseEntity<>(HttpStatus.NOT_FOUND);
	}

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