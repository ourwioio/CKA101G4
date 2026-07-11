package com.webond.member.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
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
			return "redirect:" + (location != null ? location : "/member/memberSelect_page");

		} else {
			model.addAttribute("email", email);
			model.addAttribute("errorMsgs", "您的帳號或密碼無效！");
			return "front-end/member/login";
		}
	}

	// =========================================================================
	// 📝 API 2：註冊表單送出（與 AJAX 驗證邏輯對接修正）
	// =========================================================================
	@PostMapping("/doRegister")
	public String doRegister(@Valid MemberVO memberVO, BindingResult result,
			@RequestParam(name = "otpCode", required = false) String inputOtpCode,
			@RequestParam(name = "password", required = false) String rawPassword,
			@RequestParam(name = "memberPicFile", required = false) MultipartFile memberPicFile,
			@RequestParam(name = "idImageFile", required = false) MultipartFile idImageFile,
			@RequestParam(name = "faceImageFile", required = false) MultipartFile faceImageFile,
			@RequestParam(name = "tempMemberPic", required = false) String tempMemberPic,
			@RequestParam(name = "tempIdImage", required = false) String tempIdImage,
			@RequestParam(name = "tempFaceImage", required = false) String tempFaceImage, Model model) {

		// 1. 處理密碼
		if (rawPassword == null || rawPassword.trim().isEmpty()) {
			result.rejectValue("passwordHash", "error.passwordHash", "密碼請勿空白");
		} else {
			memberVO.setPasswordHash(passwordEncoder.encode(rawPassword));
		}

		// 🟢 2. 驗證碼檢查：前端已透過 AuthController (/verify-otp) 完成第一關驗證
		// 這裡只需檢查欄位是否有填寫，避免 Redis key 提前刪除或過期導致提交失敗
		if (inputOtpCode == null || inputOtpCode.trim().isEmpty()) {
			model.addAttribute("otpError", "請輸入信箱驗證碼！");
			result.reject("error.otpCode", "請輸入信箱驗證碼");
		}

		// 3. 處理圖片
		byte[] memberPicBytes = processImageBytes(memberPicFile, tempMemberPic);
		byte[] idImageBytes = processImageBytes(idImageFile, tempIdImage);
		byte[] faceImageBytes = processImageBytes(faceImageFile, tempFaceImage);

		memberVO.setMemberPic(memberPicBytes != null ? memberPicBytes : new byte[0]);
		memberVO.setIdImage(idImageBytes);
		memberVO.setFaceImage(faceImageBytes);

		if (idImageBytes == null || idImageBytes.length == 0) {
			result.rejectValue("idImage", "error.idImage", "請務必上傳身分證件照片");
		}
		if (faceImageBytes == null || faceImageBytes.length == 0) {
			result.rejectValue("faceImage", "error.faceImage", "請務必上傳自拍人臉圖片");
		}

		// 預設帳號與實名狀態
		memberVO.setAccountStatus((byte) 0);
		memberVO.setKycStatus((byte) 0);
		memberVO.setCreatedAt(java.time.LocalDate.now());
		memberVO.setSubmittedAt(java.time.LocalDateTime.now());

		// 4. 驗證失敗處理：保留圖片暫存與原本輸入的明碼密碼/驗證碼
		if (result.hasErrors()) {
			model.addAttribute("memberVO", memberVO);
			model.addAttribute("rawPassword", rawPassword);
			model.addAttribute("inputOtpCode", inputOtpCode);
			model.addAttribute("tempMemberPic", encodeToBase64(memberPicBytes));
			model.addAttribute("tempIdImage", encodeToBase64(idImageBytes));
			model.addAttribute("tempFaceImage", encodeToBase64(faceImageBytes));
			return "front-end/member/register";
		}

		// 5. 送出資料庫寫入，並清理 Redis 驗證碼
		try {
			memberService.registerMember(memberVO);

			// 🟢 註冊成功，順便刪除 Redis 中的驗證碼
			redisService.deleteOtp(memberVO.getEmail());

			return "redirect:/member/login";
		} catch (Exception e) {
			e.printStackTrace(); // 🟢 印出 Exception 方便 Console 排查原因
			result.rejectValue("email", "error.database", "註冊失敗：Email 重複註冊或資料格式不正確！");
			model.addAttribute("memberVO", memberVO);
			model.addAttribute("rawPassword", rawPassword);
			model.addAttribute("inputOtpCode", inputOtpCode);
			model.addAttribute("tempMemberPic", encodeToBase64(memberPicBytes));
			model.addAttribute("tempIdImage", encodeToBase64(idImageBytes));
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
				return Base64.getDecoder().decode(tempBase64);
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