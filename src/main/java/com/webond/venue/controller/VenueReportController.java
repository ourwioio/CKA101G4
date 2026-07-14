package com.webond.venue.controller;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttribute;

import com.webond.employee.model.EmpService;
import com.webond.employee.model.EmployeeVO;
import com.webond.venue.model.VenueReportVO;
import com.webond.venue.service.VenueReportService;

@Controller
@RequestMapping("/venueReport")
public class VenueReportController {

	@Autowired
	VenueReportService venueReportService;

	@Autowired
	EmpService empService; // 負責員工姓名對照用

	// 檢舉處理狀態文字對照（畫面顯示用）
	private static final Map<Byte, String> STATUS_LABEL_MAP = new LinkedHashMap<>();
	static {
		STATUS_LABEL_MAP.put((byte) 0, "審核中");
		STATUS_LABEL_MAP.put((byte) 1, "通過（下架場地）");
		STATUS_LABEL_MAP.put((byte) 2, "未通過");
	}

	// 員工編號 → 姓名 對照表
	private Map<Integer, String> buildEmployeeNameMap() {
		return empService.getAll().stream()
				.collect(Collectors.toMap(EmployeeVO::getEmployeeId, EmployeeVO::getEmpName));
	}

	// ===== 列表頁 =====
	@GetMapping("/listAllVenueReport")
	public String listAll(Model model) {
		List<VenueReportVO> list = venueReportService.getAll();
		model.addAttribute("venueReportListData", list);
		model.addAttribute("statusLabelMap", STATUS_LABEL_MAP);
		model.addAttribute("employeeNameMap", buildEmployeeNameMap());
		return "back-end/venueReport/listAllVenueReport";
	}

	// ===== 依狀態查詢 =====
	@GetMapping("/listByStatus")
	public String listByStatus(@RequestParam(value = "reportStatus", required = false) Byte reportStatus, Model model) {
		List<VenueReportVO> list = (reportStatus == null) ? venueReportService.getAll()
				: venueReportService.getByStatus(reportStatus);
		model.addAttribute("venueReportListData", list);
		model.addAttribute("statusLabelMap", STATUS_LABEL_MAP);
		model.addAttribute("employeeNameMap", buildEmployeeNameMap());
		model.addAttribute("searchReportStatus", reportStatus);
		return "back-end/venueReport/listAllVenueReport";
	}

	// ===== 查詢單筆（詳細/處理頁） =====
	@GetMapping("/viewOne")
	public String viewOne(@RequestParam("venueReportId") Integer venueReportId, Model model) {
		VenueReportVO existingVenueReport = venueReportService.getOneVenueReport(venueReportId);
		model.addAttribute("existingVenueReport", existingVenueReport);
		model.addAttribute("statusLabelMap", STATUS_LABEL_MAP);
		model.addAttribute("employeeNameMap", buildEmployeeNameMap());
		return "back-end/venueReport/listOneVenueReport";
	}

	// ===== 審核通過（下架場地） =====
	@PostMapping("/approve")
	public String approve(@RequestParam("venueReportId") Integer venueReportId,
			@SessionAttribute("loginEmp") EmployeeVO loginEmp) {
		venueReportService.approve(venueReportId, loginEmp.getEmployeeId());
		return "redirect:/venueReport/listAllVenueReport";
	}

	// ===== 審核未通過 =====
	@PostMapping("/reject")
	public String reject(@RequestParam("venueReportId") Integer venueReportId,
			@SessionAttribute("loginEmp") EmployeeVO loginEmp) {
		venueReportService.reject(venueReportId, loginEmp.getEmployeeId());
		return "redirect:/venueReport/listAllVenueReport";
	}
}
