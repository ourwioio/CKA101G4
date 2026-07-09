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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
	// 🟢 【後台功能 1】審核中心主頁 (全後端主導 + 支援 activeTab 與 reportId 自動選取)
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

		// 1. 撈出所有檢舉清單
		List<MemberReportVO> list = reportSvc.getAll();
		model.addAttribute("memberReportListData", list);
		model.addAttribute("isManager", true);
		model.addAttribute("activeTab", activeTab); // 帶給 Thymeleaf 決定哪一個 Tab 亮起

		// 2. 自動決定右側面板要顯示的案件 (selectedReport)
		MemberReportVO selectedReport = null;
		
		if (reportId != null) {
			// 有指定案號，優先讀取該案號
			selectedReport = reportSvc.getOneMemberReport(reportId);
		} 
		
		// 3. 防呆機制：若未指定 reportId 或指定的案號無效，則依據目前的 Tab 預設抓取第一筆
		if (selectedReport == null && !list.isEmpty()) {
			for (MemberReportVO vo : list) {
				if ("all".equals(activeTab) || String.valueOf(vo.getReportStatus()).equals(activeTab)) {
					selectedReport = vo;
					break;
				}
			}
			// 若該 Tab 下完全沒有案件，則退回列表的第一筆
			if (selectedReport == null) {
				selectedReport = list.get(0);
			}
		}
		
		model.addAttribute("selectedReport", selectedReport);

		System.out.println("====== [Debug] 後台 /manageReport 載入成功，目前選取案號 #" 
				+ (selectedReport != null ? selectedReport.getReportId() : "無") + ", 頁籤: " + activeTab + " ======");

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
	// 🛡️ 【防呆 GET 路由】防止審核後手動 F5 重新整理導致 HTTP 400 崩潰
	// =========================================================================
	@GetMapping("/updateReportStatus")
	public String handleGetUpdateReportStatus() {
		return "redirect:/backend/memberreport/manageReport";
	}

	// =========================================================================
	// 🟢 【後台功能 4】審核提交 (執行業務邏輯，精準帶回提示訊息與鎖定分頁案件)
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

		// 4. 有驗證錯誤時，停留在原頁面並拋出錯誤訊息
		if (!errorMsgs.isEmpty()) {
			model.addAttribute("errorMsgs", errorMsgs);
			model.addAttribute("memberReportListData", reportSvc.getAll());
			model.addAttribute("selectedReport", reportSvc.getOneMemberReport(reportId));
			model.addAttribute("activeTab", "0"); // 失敗時留在待處理頁籤
			model.addAttribute("isManager", true);
			return "back-end/member/manageReport";
		}

		try {
			// 5. 呼叫 Service 執行業務邏輯 (扣點、自動停權、saveAndFlush 強制寫入)
			reportSvc.jombackProcessReport(reportId, employeeId, reportStatus, adminNote, violationPoints);
			
			// 提示訊息：透過 RedirectAttributes 在重導向後顯示
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

		// 6. 🎯【核心重導向】：帶上 reportId 與 activeTab=reportStatus，讓頁面切換到對應已結案分頁並選中該案
		return "redirect:/backend/memberreport/manageReport?reportId=" + reportId + "&activeTab=" + reportStatus;
	}

	// =========================================================================
	// 🟢 【前台功能 1】開啟前台新增檢舉案的網頁
	// =========================================================================
	@GetMapping("/addReport")
	public String showAddReportPage(ModelMap model) {
		model.addAttribute("memberReportVO", new MemberReportVO());
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
			@RequestParam("evidencePath") MultipartFile file, 
			RedirectAttributes redirectAttributes,
			ModelMap model) {
		
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

			// 送出新增
			reportSvc.addMemberReport(reporterId, reportedId, reportCategory, reportContent, evidence);
			
			redirectAttributes.addFlashAttribute("successMsg", "檢舉案已成功送出，管理員將儘速進行審核！");
			return "redirect:/backend/memberreport/addReport";
			
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