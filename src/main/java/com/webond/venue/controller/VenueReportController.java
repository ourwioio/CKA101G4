package com.webond.venue.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.webond.employee.model.EmployeeVO;
import com.webond.employee.repository.EmployeeRepository;
import com.webond.member.model.NotificationVO;
import com.webond.member.service.NotificationService;
import com.webond.venue.model.VenueOrderVO;
import com.webond.venue.model.VenueReportVO;
import com.webond.venue.model.VenueVO;
import com.webond.venue.service.VenueReportService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/venueReport")
public class VenueReportController {

	@Autowired
	private VenueReportService venueReportSvc;

	@Autowired
	private EmployeeRepository employeeRepository;

	@Autowired
	private NotificationService notificationService;

	// ===== 提供狀態對照表：0 審核中 / 1 審核通過 / 2 審核未通過 =====
	@ModelAttribute("statusLabelMap")
	protected Map<Byte, String> referenceStatusLabelMap() {
		return VenueReportService.STATUS_LABEL_MAP;
	}

	// ===== 提供員工清單，給列表頁篩選用 =====
	@ModelAttribute("employeeListData")
	protected List<EmployeeVO> referenceEmployeeList() {
		return employeeRepository.findAll();
	}

	// ===== 提供「員工編號 → 姓名」對照表，給列表頁/詳情頁顯示用 =====
	@ModelAttribute("employeeNameMap")
	protected Map<Integer, String> referenceEmployeeNameMap() {
		return employeeRepository.findAll().stream()
				.collect(Collectors.toMap(EmployeeVO::getEmployeeId, EmployeeVO::getEmpName));
	}

	// ===== 查詢全部 =====
	@GetMapping("listAllVenueReport")
	public String listAllVenueReport(ModelMap model, HttpSession session) {

		EmployeeVO loginEmp = (EmployeeVO) session.getAttribute("employeeVO");
		if (loginEmp == null) {
			return "redirect:/admin/login";
		}

		List<VenueReportVO> list = venueReportSvc.getAll();
		model.addAttribute("venueReportListData", list);
		model.addAttribute("searchStatus", null);
		model.addAttribute("searchKeyword", null);
		model.addAttribute("searchStartDate", null);
		model.addAttribute("searchEndDate", null);
		return "back-end/venueReport/listAllVenueReport";
	}

	// ===== 複合查詢：處理狀態 + 檢舉內容關鍵字 + 檢舉時間區間 =====
	@GetMapping("search")
	public String search(@RequestParam(value = "reportStatus", required = false) Byte reportStatus,
			@RequestParam(value = "keyword", required = false) String keyword,
			@RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
			@RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
			ModelMap model) {

		List<VenueReportVO> list = venueReportSvc.search(reportStatus, keyword, startDate, endDate);

		model.addAttribute("venueReportListData", list);
		model.addAttribute("searchStatus", reportStatus);
		model.addAttribute("searchKeyword", keyword);
		model.addAttribute("searchStartDate", startDate);
		model.addAttribute("searchEndDate", endDate);
		return "back-end/venueReport/listAllVenueReport";
	}

	// ===== 查看單筆內容 =====
	@GetMapping("viewOne")
	public String viewOne(@RequestParam("venueReportId") Integer venueReportId, ModelMap model) {

		VenueReportVO venueReportVO = venueReportSvc.getOneVenueReport(venueReportId);
		if (venueReportVO == null) {
			return "redirect:/venueReport/listAllVenueReport";
		}

		VenueOrderVO venueOrderVO = venueReportSvc.getVenueOrder(venueReportVO.getVenueOrderId());

		model.addAttribute("venueReportVO", venueReportVO);
		model.addAttribute("venueOrderVO", venueOrderVO);
		model.addAttribute("venueVO", venueOrderVO != null ? venueOrderVO.getVenueVO() : null);
		return "back-end/venueReport/listOneVenueReport";
	}

	// ===== 審核通過（檢舉成立） =====
	@PostMapping("approve")
	public String approve(@RequestParam("venueReportId") Integer venueReportId, RedirectAttributes redirectAttrs,
			HttpSession session) {

		EmployeeVO loginEmp = (EmployeeVO) session.getAttribute("employeeVO");
		if (loginEmp == null) {
			return "redirect:/admin/login";
		}

		venueReportSvc.approve(venueReportId, loginEmp.getEmployeeId());
		redirectAttrs.addFlashAttribute("success", "- (檢舉成立，場地已退回待審核)");

		VenueReportVO venueReportVO = venueReportSvc.getOneVenueReport(venueReportId);
		VenueOrderVO venueOrderVO = venueReportSvc.getVenueOrder(venueReportVO.getVenueOrderId());
		VenueVO venueVO = venueOrderVO.getVenueVO();

		// 通知場地主：場地遭檢舉
		NotificationVO notificationVO = new NotificationVO();
		notificationVO.setMember(venueVO.getMember());
		notificationVO.setTitle("場地遭檢舉通知");
		notificationVO.setContent("先生/小姐您好，您的場地：" + venueVO.getVenueName() + "　已遭檢舉，請重新修改您的資料再進行審核，謝謝");
		notificationVO.setNotificationType((byte) 2);
		notificationService.addNotification(notificationVO);

		// 通知檢舉人（訂單的租借方）：檢舉成立
		NotificationVO reporterNotification = new NotificationVO();
		reporterNotification.setMember(venueOrderVO.getMember());
		reporterNotification.setTitle("場地檢舉成立通知");
		reporterNotification.setContent("先生/小姐您好，您對場地：" + venueVO.getVenueName() + "　的檢舉經審核後成立，該場地已下架並退回待審核，感謝您的回報");
		reporterNotification.setNotificationType((byte) 2);
		notificationService.addNotification(reporterNotification);

		return "redirect:/venueReport/viewOne?venueReportId=" + venueReportId;
	}

	// ===== 審核未通過（檢舉不成立） =====
	@PostMapping("reject")
	public String reject(@RequestParam("venueReportId") Integer venueReportId, RedirectAttributes redirectAttrs,
			HttpSession session) {

		EmployeeVO loginEmp = (EmployeeVO) session.getAttribute("employeeVO");
		if (loginEmp == null) {
			return "redirect:/admin/login";
		}

		venueReportSvc.reject(venueReportId, loginEmp.getEmployeeId());
		redirectAttrs.addFlashAttribute("success", "- (檢舉不成立，場地狀態未變更)");

		VenueReportVO venueReportVO = venueReportSvc.getOneVenueReport(venueReportId);
		VenueOrderVO venueOrderVO = venueReportSvc.getVenueOrder(venueReportVO.getVenueOrderId());
		VenueVO venueVO = venueOrderVO.getVenueVO();

		// 只通知檢舉人（訂單的租借方）：檢舉不成立（無須通知場地主）
		NotificationVO reporterNotification = new NotificationVO();
		reporterNotification.setMember(venueOrderVO.getMember());
		reporterNotification.setTitle("場地檢舉未成立通知");
		reporterNotification.setContent("先生/小姐您好，您對場地：" + venueVO.getVenueName() + "　的檢舉經審核後不成立，該場地維持原狀態，感謝您的回報");
		reporterNotification.setNotificationType((byte) 2);
		notificationService.addNotification(reporterNotification);

		return "redirect:/venueReport/viewOne?venueReportId=" + venueReportId;
	}
}