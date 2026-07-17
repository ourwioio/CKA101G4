package com.webond.member.controller;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.webond.member.model.MemberVO;
import com.webond.member.service.MemberServiceLoie;

@RequestMapping("/admin/members")
@Controller
public class MemberBackControllerLoie {
	
	@Autowired
	private MemberServiceLoie memberService;
	
	
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
		return "redirect:/admin/members/back-end/memberList";
	}

	@GetMapping("/back-end/displayImage")
	@ResponseBody
	public ResponseEntity<byte[]> displayImage(@RequestParam("memberId") Integer memberId,
			@RequestParam("type") String type) {
		
		MemberVO memberVO = memberService.getOneMember(memberId);
		// 基礎防禦：會員不存在或圖片欄位為空則回傳 404
		if (memberVO == null || memberVO.getIdImage() == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}

		try {
			// 1. 處理身分證正反面 (JSON 格式儲存)
			if ("idFront".equals(type) || "idBack".equals(type)) {
				String jsonStr = new String(memberVO.getIdImage(), "UTF-8");
				// 根據 type 對應 JSON 中的 Key: front 或 back
				String key = "idFront".equals(type) ? "front" : "back";

				// 手動解析 JSON 字串，避免額外的 Library 依賴
				String searchKey = "\"" + key + "\":\"";
				int start = jsonStr.indexOf(searchKey);
				if (start == -1) return new ResponseEntity<>(HttpStatus.NOT_FOUND);

				start += searchKey.length();
				int end = jsonStr.indexOf("\"", start);
				String base64 = jsonStr.substring(start, end);

				// 解碼並清除 Base64 可能存在的換行符號
				byte[] imageBytes = Base64.getDecoder().decode(base64.replaceAll("\\s", ""));

				HttpHeaders headers = new HttpHeaders();
				headers.setContentType(MediaType.IMAGE_JPEG);
				return new ResponseEntity<>(imageBytes, headers, HttpStatus.OK);
			} 
			// 2. 處理人臉照片 (直接儲存的 BLOB)
			else if ("faceImage".equals(type)) {
				if (memberVO.getFaceImage() == null) return new ResponseEntity<>(HttpStatus.NOT_FOUND);
				return ResponseEntity.ok()
						.contentType(MediaType.IMAGE_JPEG)
						.body(memberVO.getFaceImage());
			} 
			// 3. 處理大頭照
			else if ("memberPic".equals(type)) {
				if (memberVO.getMemberPic() == null) return new ResponseEntity<>(HttpStatus.NOT_FOUND);
				return ResponseEntity.ok()
						.contentType(MediaType.IMAGE_JPEG)
						.body(memberVO.getMemberPic());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new ResponseEntity<>(HttpStatus.NOT_FOUND);
	}

}
