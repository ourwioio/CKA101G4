package com.webond.member.controller;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.webond.member.model.MemberReportVO;
import com.webond.member.service.MemberReportService;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/backend/memberreport")
public class MemberReportController {

	@Autowired
	private MemberReportService reportSvc;

	// =========================================================================
	// 🔑 【私有權限檢查工具方法】 (免登入測試版：全面綠燈放行！)
	// =========================================================================
	private boolean hasPermissionFive(HttpSession session) {
		return true;
	}

	// =========================================================================
	// 🟢 【後台功能 1】權限五管理員專用審核後台進入點
	// =========================================================================
	@GetMapping("/manageReport")
	public String manageReport(HttpSession session, ModelMap model) {

		if (!hasPermissionFive(session)) {
			model.addAttribute("errorMsg", "權限不足！此頁面僅限高級審核主管存取。");
			return "front-end/member/memberreport/error_page";
		}

		List<MemberReportVO> list = reportSvc.getAll();
		model.addAttribute("memberReportListData", list);
		model.addAttribute("isManager", true);

		System.out.println("====== [Debug] 權限五專用管理後台 /manageReport 載入成功 ======");

		// 🎯 對齊資料夾結構：templates/back-end/member/manageReport.html
		return "back-end/member/manageReport";
	}

	// =========================================================================
	// 🟢 【後台功能 2】後台總表進入點 - (保留原總表路由，供一般查看使用)
	// =========================================================================
	@GetMapping("/listAllReport")
	public String listAllReport(HttpSession session, ModelMap model) {
		List<MemberReportVO> list = reportSvc.getAll();
		model.addAttribute("memberReportListData", list);

		boolean isManager = hasPermissionFive(session);
		model.addAttribute("isManager", isManager);

		System.out.println("====== [Debug] 後台檢舉總表開放，免登入測試狀態，目前管理者權限: " + isManager + " ======");
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
				byte[] imageBytes = memberReportVO.getEvidence();
				out.write(imageBytes);
			} else {
				System.out.println("[Warn] 案號 #" + reportId + " 查無圖片資料。");
			}
		} catch (IOException e) {
			System.out.println("[Error] 讀取圖片發生串流異常: " + e.getMessage());
		}
	}

	// =========================================================================
	// 🟢 【後台功能 4】新版權限五管理員送出審核結果
	// =========================================================================
	@PostMapping("/updateReportStatus")
	public String updateReportStatus(@RequestParam("reportId") Integer reportId,
			@RequestParam("reportStatus") Integer reportStatus, @RequestParam("employeeId") String employeeIdStr,
			@RequestParam(value = "violationPoints", required = false) String violationPointsStr,
			@RequestParam(value = "adminNote", required = false) String adminNote, HttpSession session,
			ModelMap model) {

		if (!hasPermissionFive(session)) {
			model.addAttribute("errorMsg", "權限不足！只有審核主管可以提交審核結果.");
			return "front-end/member/memberreport/error_page";
		}

		List<String> errorMsgs = new LinkedList<>();
		model.addAttribute("errorMsgs", errorMsgs);

		// 1. 驗證員工編號
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

		// 2. 驗證違規扣點
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

		// 3. 處理駁回案件的點數歸零防呆
		if (Integer.valueOf(2).equals(reportStatus)) {
			violationPoints = 0;
		}

		// 4. 有錯誤則返回後台頁面
		if (!errorMsgs.isEmpty()) {
			model.addAttribute("memberReportListData", reportSvc.getAll());
			model.addAttribute("isManager", true);
			return "back-end/member/manageReport"; 
		}

		try {
			reportSvc.jombackProcessReport(reportId, employeeId, reportStatus, adminNote, violationPoints);
		} catch (Exception e) {
			errorMsgs.add("資料庫更新失敗：" + e.getMessage());
			model.addAttribute("memberReportListData", reportSvc.getAll());
			model.addAttribute("isManager", true);
			return "back-end/member/manageReport"; 
		}

		// 審核成功重導向回管理首頁
		return "redirect:/backend/memberreport/manageReport";
	}

	// =========================================================================
	// 🟢 【前台功能 1】開啟前台新增檢舉案的網頁 (🎯 解決 405 Method Not Allowed 錯誤)
	// =========================================================================
	@GetMapping("/addReport")
	public String showAddReportPage(ModelMap model) {
		// 預先帶入空的 VO 以供網頁基本物件綁定防錯
		model.addAttribute("memberReportVO", new MemberReportVO());
		
		// 🎯 對齊前台資料夾結構
		return "front-end/member/memberreport/addReport";
	}

	// =========================================================================
	// 🟢 【前台功能 2】新使用者送出單筆檢舉案資料驗證
	// =========================================================================
	@PostMapping("/addReport")
	public String addReport(@RequestParam(value = "reporterId", required = false) String reporterIdStr,
			@RequestParam(value = "reportedId", required = false) String reportedIdStr,
			@RequestParam(value = "reportCategory", required = false) String reportCategoryStr,
			@RequestParam(value = "reportContent", required = false) String reportContent,
			@RequestParam("evidencePath") MultipartFile file, ModelMap model) {
		List<String> errorMsgs = new LinkedList<>();
		model.addAttribute("errorMsgs", errorMsgs);
		MemberReportVO memberReportVO = new MemberReportVO();
		try {
			Integer reporterId = null;
			if (reporterIdStr == null || reporterIdStr.trim().length() == 0) {
				errorMsgs.add("檢舉人編號：請勿空白");
			} else {
				try {
					reporterId = Integer.valueOf(reporterIdStr.trim());
				} catch (NumberFormatException e) {
					errorMsgs.add("檢舉人編號格式錯誤");
				}
			}

			Integer reportedId = null;
			if (reportedIdStr == null || reportedIdStr.trim().length() == 0) {
				errorMsgs.add("被檢舉人編號：請勿空白");
			} else {
				try {
					reportedId = Integer.valueOf(reportedIdStr.trim());
				} catch (NumberFormatException e) {
					errorMsgs.add("被檢舉人編號格式錯誤");
				}
			}

			Integer reportCategory = null;
			if (reportCategoryStr == null || reportCategoryStr.trim().length() == 0) {
				errorMsgs.add("檢舉類別：請選擇原因");
			} else {
				try {
					reportCategory = Integer.valueOf(reportCategoryStr.trim());
				} catch (NumberFormatException e) {
					errorMsgs.add("檢舉類別格式錯誤");
				}
			}

			if (reportContent == null || reportContent.trim().length() == 0) {
				errorMsgs.add("詳細內容描述：請勿空白");
			}

			byte[] evidence = null;
			if (file == null || file.isEmpty()) {
				errorMsgs.add("請上傳證據截圖");
			} else {
				String contentType = file.getContentType();
				if (contentType == null || !contentType.startsWith("image/")) {
					errorMsgs.add("檔案必須是圖片格式");
				} else {
					evidence = file.getBytes();
				}
			}

			if (!errorMsgs.isEmpty()) {
				model.addAttribute("memberReportVO", memberReportVO);
				return "front-end/member/memberreport/addReport";
			}

			reportSvc.addMemberReport(reporterId, reportedId, reportCategory, reportContent, evidence);
			return "redirect:/backend/memberreport/select_page";
		} catch (Exception e) {
			errorMsgs.add("送出檢舉失敗：" + e.getMessage());
			model.addAttribute("memberReportVO", memberReportVO);
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