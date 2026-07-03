package com.webond.member.controller;

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

@Controller
@RequestMapping("/memberreport") // 🎯 對齊原網址 /memberreport
public class MemberReportController {

	@Autowired
	private MemberReportService reportSvc; // 雇用 Service 大總管
	
	@Autowired
    private MemberReportService memberReportSvc;
	
	
	// 🟢 【新增首頁進入點】使用者進到首頁時，Controller 先去撈全部檢舉案供下拉選單使用
	@GetMapping("/select_page")
	public String select_page(ModelMap model) {
		// 呼叫 Service 拿全部檢舉案
		List<MemberReportVO> list = reportSvc.getAll();
		// 打包進背包，給下拉選單（th:each）使用
		model.addAttribute("memberReportListData", list);
		
		// 指向 templates/memberreport/select_page.html
		return "memberreport/select_page";
	}

	// =========================================================================
	// 【功能 1】單筆查詢某案詳細內容 (對應原 getOne_For_Display)
	// =========================================================================
	@PostMapping("/getOne_For_Display")
	public String getOne_For_Display(
			@RequestParam(value = "reportId", required = false) String str, 
			ModelMap model) {

		List<String> errorMsgs = new LinkedList<>();
		model.addAttribute("errorMsgs", errorMsgs);

		/*************************** 1.接收請求參數 - 輸入格式的錯誤處理 **********************/
		if (str == null || (str.trim()).length() == 0) {
			errorMsgs.add("請輸入檢舉案號");
		}

		if (!errorMsgs.isEmpty()) {
			return "memberreport/select_page"; // 🟢 導向 Thymeleaf 的 select_page.html
		}

		Integer reportId = null;
		try {
			reportId = Integer.valueOf(str.trim());
		} catch (Exception e) {
			errorMsgs.add("檢舉案號格式不正確，請輸入數字");
		}

		if (!errorMsgs.isEmpty()) {
			return "memberreport/select_page";
		}

		/*************************** 2.開始查詢資料 *****************************************/
		MemberReportVO memberReportVO = reportSvc.getOneMemberReport(reportId);
		if (memberReportVO == null) {
			errorMsgs.add("查無此案號資料");
		}

		if (!errorMsgs.isEmpty()) {
			return "memberreport/select_page";
		}

		/*************************** 3.查詢完成,準備轉交 *************************************/
		model.addAttribute("memberReportVO", memberReportVO); 
		return "memberreport/listOneReport"; // 🟢 導向 listOneReport.html
	}

	// =========================================================================
	// 【功能 2】點擊「審核」按鈕，撈出舊資料去修改頁面 (對應原 getOne_For_Update)
	// =========================================================================
	@PostMapping("/getOne_For_Update")
	public String getOne_For_Update(@RequestParam("reportId") Integer reportId, ModelMap model) {

		/*************************** 1.開始查詢資料 ****************************************/
		MemberReportVO memberReportVO = reportSvc.getOneMemberReport(reportId);

		/*************************** 2.查詢完成,準備轉交 ************************************/
		model.addAttribute("memberReportVO", memberReportVO); 
		return "memberreport/update_report_input"; // 🟢 導向後台審核輸入頁面
	}

	// =========================================================================
	// 【功能 3】管理員送出審核結果 (對應原 update)
	// =========================================================================
	@PostMapping("/update")
	public String update(
			@RequestParam("reportId") Integer reportId,
			@RequestParam(value = "employeeId", required = false) String employeeIdStr,
			@RequestParam("reportStatus") Integer reportStatus,
			@RequestParam(value = "adminNote", required = false) String adminNote,
			@RequestParam(value = "violationPoints", required = false) String violationPointsStr,
			ModelMap model) {

		List<String> errorMsgs = new LinkedList<>();
		model.addAttribute("errorMsgs", errorMsgs);

		/*************************** 1.接收請求參數 - 輸入格式的錯誤處理 **********************/
		Integer employeeId = null;
		try {
			if (employeeIdStr == null || employeeIdStr.trim().isEmpty()) {
				errorMsgs.add("員工編號請勿空白");
			} else {
				employeeId = Integer.valueOf(employeeIdStr.trim());
			}
		} catch (NumberFormatException e) {
			errorMsgs.add("員工編號請填寫數字");
		}

		if (adminNote == null || adminNote.trim().length() == 0) {
			errorMsgs.add("管理員處理備註: 請勿空白");
		}

		Integer violationPoints = null;
		try {
			if (violationPointsStr == null || violationPointsStr.trim().isEmpty()) {
				errorMsgs.add("違規點數請勿空白");
			} else {
				violationPoints = Integer.valueOf(violationPointsStr.trim());
				if (violationPoints < 0) {
					errorMsgs.add("違規扣點不能為負數");
				}
			}
		} catch (NumberFormatException e) {
			violationPoints = 0;
			errorMsgs.add("違規點數請填數字");
		}

		// 為了防止退件時畫面錯誤，先利用原 ID 撈出原本的完整狀態
		MemberReportVO memberReportVO = reportSvc.getOneMemberReport(reportId);
		
		// 若有格式錯誤，踢回審核輸入頁面，並回填管理員剛剛打的內容
		if (!errorMsgs.isEmpty()) {
			model.addAttribute("memberReportVO", memberReportVO);
			return "memberreport/update_report_input"; 
		}

		/*************************** 2.開始修改資料 *****************************************/
		memberReportVO = reportSvc.jombackProcessReport(reportId, employeeId, reportStatus, adminNote, violationPoints);

		/*************************** 3.修改完成,準備轉交 *************************************/
		model.addAttribute("memberReportVO", memberReportVO); 
		return "redirect:/memberreport/listAllReport"; // 🟢 審核完畢用 redirect 刷回大表格，防止 F5 重複觸發
	}

	// =========================================================================
	// 【功能 4】前台會員送出全新檢舉案 (對應原 insert)
	// =========================================================================
	@PostMapping("/insert")
	public String insert(
			@RequestParam(value = "reporterId", required = false) String reporterIdStr,
			@RequestParam(value = "reportedId", required = false) String reportedIdStr,
			@RequestParam(value = "reportCategory", required = false) String reportCategoryStr,
			@RequestParam(value = "reportContent", required = false) String reportContent,
			@RequestParam("evidencePath") MultipartFile file, // 📸 改用 Spring 的 MultipartFile 接收圖片
			ModelMap model) {

		List<String> errorMsgs = new LinkedList<>();
		model.addAttribute("errorMsgs", errorMsgs);

		MemberReportVO memberReportVO = new MemberReportVO();

		try {
			/*********************** 1. 接收請求參數與格式驗證 *************************/
			Integer reporterId = null;
			if (reporterIdStr == null || reporterIdStr.trim().length() == 0) {
				errorMsgs.add("檢舉人編號：請勿空白");
			} else {
				try {
					reporterId = Integer.valueOf(reporterIdStr.trim());
				} catch (NumberFormatException e) {
					errorMsgs.add("檢舉人編號格式錯誤，必須是數字");
				}
			}

			Integer reportedId = null;
			if (reportedIdStr == null || reportedIdStr.trim().length() == 0) {
				errorMsgs.add("被檢舉人編號：請勿空白");
			} else {
				try {
					reportedId = Integer.valueOf(reportedIdStr.trim());
				} catch (NumberFormatException e) {
					errorMsgs.add("被檢舉人編號格式錯誤，必須是數字");
				}
			}

			Integer reportCategory = null;
			if (reportCategoryStr == null || reportCategoryStr.trim().length() == 0) {
				errorMsgs.add("檢舉類別：請選擇檢舉原因");
			} else {
				try {
					reportCategory = Integer.valueOf(reportCategoryStr.trim());
					if (reportCategory < 0 || reportCategory > 3) {
						errorMsgs.add("檢舉類別格式錯誤");
					}
				} catch (NumberFormatException e) {
					errorMsgs.add("檢舉類別格式錯誤");
				}
			}

			if (reportContent == null || reportContent.trim().length() == 0) {
				errorMsgs.add("詳細內容描述：請勿空白");
			}

			// 圖片檔案檢查
			byte[] evidence = null;
			if (file == null || file.isEmpty()) {
				errorMsgs.add("請上傳證據截圖，以利後台查證。");
			} else {
				String contentType = file.getContentType();
				if (contentType == null || !contentType.startsWith("image/")) {
					errorMsgs.add("證據檔案必須是圖片格式。");
				} else {
					// 🟢 驗證通過，直接讀取成 byte[] 二進位資料！完全不用寫硬碟存檔路徑囉！
					evidence = file.getBytes();
				}
			}

			// 封裝成臨時外殼，以便失敗時回填網頁
			// (注意：此處因應 Service 接收型態，回填先用單純欄位或看網頁端接收方式)
			if (!errorMsgs.isEmpty()) {
				model.addAttribute("memberReportVO", memberReportVO);
				return "memberreport/addReport";
			}

			/*********************** 2. 開始新增資料 ***********************************/
			memberReportSvc.addMemberReport(reporterId, reportedId, reportCategory, reportContent, evidence);

			/*********************** 3. 新增完成，轉交成功頁面 *************************/
			return "redirect:/memberreport/select_page"; // 🟢 成功後重導向回首頁

		} catch (Exception e) {
			errorMsgs.add("送出檢舉失敗：" + e.getMessage());
			model.addAttribute("memberReportVO", memberReportVO);
			return "memberreport/addReport";
		}
	}

	// =========================================================================
	// 【功能 5】刪除某一筆資料 (對應原 delete)
	// =========================================================================
	@PostMapping("/delete")
	public String delete(@RequestParam("reportId") Integer reportId, ModelMap model) {

		/*************************** 1.開始刪除資料 ***************************************/
		reportSvc.deleteMemberReport(reportId);

		/*************************** 2.刪除完成,重導向大表格 *******************************/
		return "redirect:/memberreport/listAllReport";
	}
	
	// =========================================================================
	// 🟢 【額外好康補上】進入後台總表的進入點 (對應原 listAllReport.jsp)
	// =========================================================================
	@GetMapping("/listAllReport")
	public String listAllReport(ModelMap model) {
		List<MemberReportVO> list = reportSvc.getAll();
		model.addAttribute("memberReportListData", list);
		return "memberreport/listAllReport";
	}
}
