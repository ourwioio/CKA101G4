package com.webond.member.controller;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

// 🎯 匯入你的 DTO
import com.webond.member.model.AddMemberReportDTO;
import com.webond.member.model.MemberReportVO;
import com.webond.member.service.MemberReportService;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/backend/memberreport")
public class MemberReportController {

	@Autowired
	private MemberReportService reportSvc;

	// =========================================================================
	// 🔑 【私有權限檢查工具方法】 (免登入測試版)
	// =========================================================================
	private boolean hasPermissionFive(HttpSession session) {
		return true;
	}

	// =========================================================================
	// 🟢 【後台功能 1】審核中心主頁
	// =========================================================================
	@GetMapping("/manageReport")
	public String manageReport(
			@RequestParam(value = "reportId", required = false) Integer reportId,
			@RequestParam(value = "activeTab", defaultValue = "all") String activeTab,
			HttpSession session, ModelMap model) {

		if (!hasPermissionFive(session)) {
			model.addAttribute("errorMsg", "權限不足！此頁面僅限高級審核主管存取。");
			return "front-end/member/memberreport/error_page";
		}

		List<MemberReportVO> list = reportSvc.getAll();
		model.addAttribute("memberReportListData", list);
		model.addAttribute("isManager", true);
		model.addAttribute("activeTab", activeTab);

		MemberReportVO selectedReport = null;
		if (reportId != null) {
			selectedReport = reportSvc.getOneMemberReport(reportId);
		} 

		if (selectedReport == null && !list.isEmpty()) {
			for (MemberReportVO vo : list) {
				if ("all".equals(activeTab) || String.valueOf(vo.getReportStatus()).equals(activeTab)) {
					selectedReport = vo;
					break;
				}
			}
			if (selectedReport == null) {
				selectedReport = list.get(0);
			}
		}
		
		model.addAttribute("selectedReport", selectedReport);
		return "back-end/member/manageReport";
	}

	// =========================================================================
	// 🟢 【後台功能 2】後台總表進入點
	// =========================================================================
	@GetMapping("/listAllReport")
	public String listAllReport(HttpSession session, ModelMap model) {
		List<MemberReportVO> list = reportSvc.getAll();
		model.addAttribute("memberReportListData", list);
		model.addAttribute("isManager", hasPermissionFive(session));
		return "front-end/member/memberreport/listAllReport";
	}

	// =========================================================================
	// 📸 【後台功能 3】影像資料串流讀取器
	// =========================================================================
	@GetMapping("/DBGifReader")
	public void dbGifReader(@RequestParam("reportId") Integer reportId, HttpServletResponse response) {
		response.setContentType("image/gif");
		try (ServletOutputStream out = response.getOutputStream()) {
			MemberReportVO memberReportVO = reportSvc.getOneMemberReport(reportId);
			if (memberReportVO != null && memberReportVO.getEvidence() != null) {
				out.write(memberReportVO.getEvidence());
			}
		} catch (IOException e) {
			System.out.println("[Error] 讀取圖片發生串流異常: " + e.getMessage());
		}
	}

	// =========================================================================
	// 🛡️ 【防呆 GET 路由】防止 F5 重複提交
	// =========================================================================
	@GetMapping("/updateReportStatus")
	public String handleGetUpdateReportStatus() {
		return "redirect:/backend/memberreport/manageReport";
	}

	// =========================================================================
	// 🟢 【後台功能 4】審核提交
	// =========================================================================
	@PostMapping("/updateReportStatus")
	public String updateReportStatus(@RequestParam("reportId") Integer reportId,
			@RequestParam("reportStatus") Integer reportStatus, 
			@RequestParam("employeeId") String employeeIdStr,
			@RequestParam(value = "violationPoints", required = false) String violationPointsStr,
			@RequestParam(value = "adminNote", required = false) String adminNote, 
			HttpSession session,
			RedirectAttributes redirectAttributes,
			ModelMap model) {

		if (!hasPermissionFive(session)) {
			model.addAttribute("errorMsg", "權限不足！只有審核主管可以提交審核結果.");
			return "front-end/member/memberreport/error_page";
		}

		List<String> errorMsgs = new LinkedList<>();

		Integer employeeId = null;
		if (employeeIdStr == null || employeeIdStr.trim().isEmpty()) {
			errorMsgs.add("經辦員工編號請勿空白");
		} else {
			try {
				employeeId = Integer.valueOf(employeeIdStr.trim());
			} catch (NumberFormatException e) {
				errorMsgs.add("員工編號格式不正確，請輸入數字");
			}
		}

		Integer violationPoints = 0;
		try {
			if (violationPointsStr != null && !violationPointsStr.trim().isEmpty()) {
				violationPoints = Integer.valueOf(violationPointsStr.trim());
				if (violationPoints < 0 || violationPoints > 100) {
					errorMsgs.add("裁決扣點範圍必須介於 0 至 100 之間");
				}
			}
		} catch (NumberFormatException e) {
			errorMsgs.add("違規點數請填數字");
		}

		if (Integer.valueOf(2).equals(reportStatus)) {
			violationPoints = 0;
		}

		if (!errorMsgs.isEmpty()) {
			model.addAttribute("errorMsgs", errorMsgs);
			model.addAttribute("memberReportListData", reportSvc.getAll());
			model.addAttribute("selectedReport", reportSvc.getOneMemberReport(reportId));
			model.addAttribute("activeTab", "0");
			model.addAttribute("isManager", true);
			return "back-end/member/manageReport";
		}

		try {
			reportSvc.jombackProcessReport(reportId, employeeId, reportStatus, adminNote, violationPoints);
			String statusText = (reportStatus == 1) ? "審核成立 (扣除 " + violationPoints + " 點)" : "審核駁回";
			redirectAttributes.addFlashAttribute("successMsg", "案號 #" + reportId + " 已成功處理為【" + statusText + "】！");
		} catch (Exception e) {
			errorMsgs.add("資料庫更新失敗：" + e.getMessage());
			model.addAttribute("errorMsgs", errorMsgs);
			model.addAttribute("memberReportListData", reportSvc.getAll());
			model.addAttribute("selectedReport", reportSvc.getOneMemberReport(reportId));
			model.addAttribute("activeTab", "0");
			model.addAttribute("isManager", true);
			return "back-end/member/manageReport";
		}

		return "redirect:/backend/memberreport/manageReport?reportId=" + reportId + "&activeTab=" + reportStatus;
	}

	// =========================================================================
	// 🟢 【前台功能 1】開啟前台新增檢舉案網頁
	// =========================================================================
	@GetMapping("/addReport")
	public String showAddReportPage(ModelMap model) {
		// 🎯 傳給前端綁定的改為 AddMemberReportDTO
		model.addAttribute("addMemberReportDTO", new AddMemberReportDTO());
		return "front-end/member/memberreport/addReport";
	}

	// =========================================================================
	// 🟢 【前台功能 2】送出檢舉案資料驗證 (支援多圖 MultipartFile[])
	// =========================================================================
	@PostMapping("/addReport")
	public String addReport(
	        @Valid @ModelAttribute("addMemberReportDTO") AddMemberReportDTO dto,
	        BindingResult result,
	        RedirectAttributes redirectAttributes,
	        ModelMap model) {

	    MultipartFile[] files = dto.getEvidencePath();
	    boolean hasValidFile = false;

	    // 1. 檢查圖片陣列 (不可用 dto.getEvidencePath().isEmpty())
	    if (files != null && files.length > 0) {
	        for (MultipartFile file : files) {
	            if (!file.isEmpty()) {
	                hasValidFile = true;
	                String contentType = file.getContentType();
	                if (contentType == null || !contentType.startsWith("image/")) {
	                    result.addError(new FieldError("addMemberReportDTO", "evidencePath", "上傳的檔案必須全為圖片格式"));
	                    break;
	                }
	            }
	        }
	    }

	    if (!hasValidFile) {
	        result.addError(new FieldError("addMemberReportDTO", "evidencePath", "請至少上傳一張證據截圖"));
	    }

	    // 2. 若有驗證錯誤，直接 return 頁面（這時欄位下方紅字錯誤就會正常顯現）
	    if (result.hasErrors()) {
	        return "front-end/member/memberreport/addReport";
	    }

	    // 3. 驗證完全通過，執行新增
	    try {
	        // 取出第一張照片作為主圖傳給 Service（或者依你的業務需求處理多圖存檔）
	        byte[] evidence = null;
	        for (MultipartFile file : files) {
	            if (!file.isEmpty()) {
	                evidence = file.getBytes();
	                break;
	            }
	        }

	        reportSvc.addMemberReport(
	            dto.getReporterId(), 
	            dto.getReportedId(), 
	            dto.getReportCategory(), 
	            dto.getReportContent(), 
	            evidence
	        );

	        redirectAttributes.addFlashAttribute("successMsg", "檢舉案已成功送出，管理員將儘速進行審核！");
	        return "redirect:/backend/memberreport/addReport";

	    } catch (Exception e) {
	        model.addAttribute("errorMsg", "送出檢舉失敗：" + e.getMessage());
	        return "front-end/member/memberreport/addReport";
	    }
	}
	// =========================================================================
	// 🟢 其他輔助頁面進入點
	// =========================================================================
	@GetMapping("/select_page")
	public String select_page(HttpSession session, ModelMap model) {
		List<MemberReportVO> list = reportSvc.getAll();
		model.addAttribute("memberReportListData", list);
		model.addAttribute("isManager", hasPermissionFive(session));
		return "front-end/member/memberreport/select_page";
	}

	@PostMapping("/getOne_For_Display")
	public String getOne_For_Display(@RequestParam(value = "reportId", required = false) String str,
			HttpSession session, ModelMap model) {
		List<String> errorMsgs = new LinkedList<>();
		model.addAttribute("errorMsgs", errorMsgs);
		if (str == null || (str.trim()).length() == 0) {
			errorMsgs.add("請輸入檢舉案號");
		}
		if (!errorMsgs.isEmpty()) {
			return "front-end/member/memberreport/select_page";
		}

		Integer reportId = null;
		try {
			reportId = Integer.valueOf(str.trim());
		} catch (Exception e) {
			errorMsgs.add("檢舉案號格式不正確，請輸入數字");
		}
		if (!errorMsgs.isEmpty()) {
			return "front-end/member/memberreport/select_page";
		}

		MemberReportVO memberReportVO = reportSvc.getOneMemberReport(reportId);
		if (memberReportVO == null) {
			errorMsgs.add("查無此案號資料");
		}
		if (!errorMsgs.isEmpty()) {
			return "front-end/member/memberreport/select_page";
		}

		model.addAttribute("isManager", hasPermissionFive(session));
		model.addAttribute("memberReportVO", memberReportVO);
		return "front-end/member/memberreport/listOneReport";
	}
}