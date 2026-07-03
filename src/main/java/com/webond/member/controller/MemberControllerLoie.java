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
	public String logout(Model model, HttpSession session){
		session.invalidate();
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
	 * 📝 處理註冊與 KYC 實名認證請求
	 */
	@PostMapping("/doRegister")
	public String doRegister(
			@Valid MemberVO memberVO, 
			BindingResult result,     
			@RequestParam(name = "password") String rawPassword, 
			@RequestParam(name = "memberPicFile") MultipartFile memberPicFile,
			@RequestParam(name = "idImageFile") MultipartFile idImageFile,
			@RequestParam(name = "faceImageFile") MultipartFile faceImageFile,
			Model model) {

		System.out.println("==== [Debug] 接收到註冊請求！Email: " + memberVO.getEmail() + " ====");

		if (rawPassword == null || rawPassword.trim().isEmpty()) {
			result.rejectValue("passwordHash", "error.passwordHash", "密碼請勿空白");
		} else {
			memberVO.setPasswordHash(passwordEncoder.encode(rawPassword)); 
		}

		try {
			if (memberPicFile != null && !memberPicFile.isEmpty()) {
				memberVO.setMemberPic(memberPicFile.getBytes());
			} else {
				memberVO.setMemberPic(new byte[0]); 
			}
			
			if (idImageFile != null && !idImageFile.isEmpty()) {
				memberVO.setIdImage(idImageFile.getBytes());
			} else {
				result.rejectValue("idImage", "error.idImage", "請務必上傳身分證件照片");
			}
			
			if (faceImageFile != null && !faceImageFile.isEmpty()) {
				memberVO.setFaceImage(faceImageFile.getBytes());
			} else {
				result.rejectValue("faceImage", "error.faceImage", "請務必上傳自拍人臉圖片");
			}
		} catch (IOException e) {
			result.rejectValue("memberPic", "error.memberPic", "圖片處理發生錯誤: " + e.getMessage());
		}

		memberVO.setAccountStatus((byte) 0);                               
		memberVO.setKycStatus((byte) 0);                                   
		memberVO.setCreatedAt(java.time.LocalDate.now());                  
		memberVO.setSubmittedAt(java.time.LocalDateTime.now());            

		if (result.hasErrors()) {
			System.out.println("==== [Debug] 註冊驗證不通過，已被拒絕！錯誤數量: " + result.getErrorCount() + " ====");
			model.addAttribute("memberVO", memberVO);
			return "front-end/member/register";
		}

		try {
			memberService.registerMember(memberVO); 
			return "redirect:/member/login";
		} catch (Exception e) {
			result.rejectValue("email", "error.database", "註冊失敗：Email 重複註冊或欄位長度超出限制！");
			model.addAttribute("memberVO", memberVO);
			return "front-end/member/register";
		}
	}

	/**
	 * 📊 4. 顯示所有會員大總表（新增：關鍵字搜尋、檢舉點數高到低排序、自動停權提醒高亮）
	 */
	@GetMapping("/back-end/memberList")
	public String memberList(@RequestParam(name = "keyword", required = false) String keyword, Model model) {
		List<MemberVO> allMembers;
		
		// 判斷是否有輸入關鍵字搜尋
		if (keyword != null && !keyword.trim().isEmpty()) {
			allMembers = memberService.searchMembers(keyword.trim());
			model.addAttribute("keyword", keyword.trim());
		} else {
			allMembers = memberService.getAllMembers(); 
		}
		
		// 🎯 新增貼心排序：將全體會員轉為可變動 List，並依據檢舉點數 (reportPoints) 從大到小降冪排序
		List<MemberVO> sortedMembers = new ArrayList<>(allMembers);
		sortedMembers.sort((m1, m2) -> {
			int p1 = m1.getReportPoints() != null ? m1.getReportPoints() : 0;
			int p2 = m2.getReportPoints() != null ? m2.getReportPoints() : 0;
			return Integer.compare(p2, p1); // 點數多的排前面
		});
		
		model.addAttribute("allMembers", sortedMembers);
		return "back-end/member/memberList"; 
	}

	/**
	 * ⛔ 5. 新增管理功能：手動停權 / 恢復會員帳號權限
	 */
	@PostMapping("/back-end/toggleStatus")
	public String toggleStatus(@RequestParam("memberId") Integer memberId,
	                           @RequestParam("newStatus") Byte newStatus,
	                           RedirectAttributes redirectAttributes) {
		
		MemberVO memberVO = memberService.getOneMember(memberId);
		if (memberVO != null) {
			memberVO.setAccountStatus(newStatus);
			
			// 🎯 貼心商業邏輯：如果管理員點擊「恢復權限」(newStatus=1)，主動幫會員把檢舉點數歸零，避免一放行又立刻觸發自動停權
			if (newStatus == 1) {
				memberVO.setReportPoints(0);
			}
			
			// 呼叫帶有「滿 5 點自動停權防禦機制」的 Service 更新方法
			memberService.updateMember(memberVO);
			
			String msg = (newStatus == 3) ? "該會員已被成功停權！" : "該會員帳號已恢復正常使用，檢舉點數已重置歸零！";
			redirectAttributes.addFlashAttribute("successMsg", msg);
		} else {
			redirectAttributes.addFlashAttribute("errorMsg", "找不到該會員資料！");
		}
		
		return "redirect:/member/back-end/memberList";
	}

	// ==========================================
	// 🎯 後台：KYC 綜合審核功能面板 (方案 A 模式)
	// ==========================================

	/**
	 * 🖼️ 1. 圖片讀取 API
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
	 * 🪪 2. 綜合審核面板
	 */
	@GetMapping("/back-end/kycApproval")
	public String kycList(Model model) {
		List<MemberVO> pendingList = memberService.getMembersByStatus((byte) 0); 
		List<MemberVO> passList = memberService.getMembersByStatus((byte) 1);    
		List<MemberVO> failList = memberService.getMembersByStatus((byte) 2);    
		
		model.addAttribute("pendingList", pendingList);
		model.addAttribute("passList", passList);
		model.addAttribute("failList", failList);
		
		return "back-end/member/kycApproval"; 
	}

	/**
	 * ⚖️ 3. 處理審核結果
	 */
	@PostMapping("/back-end/reviewKyc")
	public String reviewKyc(@RequestParam("memberId") Integer memberId,
	                        @RequestParam("status") Byte status,
	                        RedirectAttributes redirectAttributes) {
		
		MemberVO memberVO = memberService.getOneMember(memberId);
		if (memberVO != null) {
			memberVO.setAccountStatus(status); 
			memberVO.setKycStatus(status); 
			
			memberService.updateMember(memberVO); 
			redirectAttributes.addFlashAttribute("successMsg", "會員 [ID: " + memberId + "] 審核操作成功！");
		} else {
			redirectAttributes.addFlashAttribute("errorMsg", "找不到該會員資料！");
		}
		
		return "redirect:/member/back-end/kycApproval"; 
	}
}