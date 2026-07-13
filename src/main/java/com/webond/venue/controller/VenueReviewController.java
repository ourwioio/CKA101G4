package com.webond.venue.controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.webond.employee.model.EmployeeVO;
import com.webond.employee.repository.EmployeeRepository;
import com.webond.venue.model.VenueReviewVO;
import com.webond.venue.model.VenueVO;
import com.webond.venue.service.VenueReviewService;
import com.webond.venue.service.VenueService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/venueReview")
public class VenueReviewController {

	@Autowired
	private VenueReviewService venueReviewSvc;

	@Autowired
	private EmployeeRepository employeeRepository;
	
	@Autowired
	VenueService venueService;

	// ===== 新增：顯示表單 =====
	@GetMapping("addVenueReview")
	public String addVenueReview(ModelMap model) {
		model.addAttribute("venueReviewVO", new VenueReviewVO());
		return "back-end/venueReview/addVenueReview";
	}

	// ===== 新增：送出表單 =====
	@PostMapping("insert")
	public String insert(@Valid @ModelAttribute("venueReviewVO") VenueReviewVO venueReviewVO, BindingResult result,
			ModelMap model) {

		/*************************** 1.接收請求參數 - 輸入格式的錯誤處理 ************************/
		if (result.hasErrors()) {
			return "back-end/venueReview/addVenueReview";
		}

		/*************************** 2.開始新增資料 *****************************************/
		venueReviewSvc.addVenueReview(venueReviewVO);

		/*************************** 3.新增完成,準備轉交(Send the Success view) **************/
		model.addAttribute("success", "- (新增成功)");
		return "redirect:/venueReview/listAllVenueReview";
	}

	// ===== 查詢單筆（供修改用） =====
	@PostMapping("getOne_For_Update")
	public String getOneForUpdate(@RequestParam("venueReviewId") Integer venueReviewId, ModelMap model) {
		/*************************** 1.接收請求參數 - 輸入格式的錯誤處理 ************************/
		/*************************** 2.開始查詢資料 *****************************************/
		VenueReviewVO venueReviewVO = venueReviewSvc.getOneVenueReview(venueReviewId);

		/*************************** 3.查詢完成,準備轉交(Send the Success view) **************/
		model.addAttribute("venueReviewVO", venueReviewVO);
		return "back-end/venueReview/update_venueReview_input";
	}

	// ===== 修改 =====
	@PostMapping("update")
	public String update(@Valid @ModelAttribute("venueReviewVO") VenueReviewVO venueReviewVO, BindingResult result,
			ModelMap model, RedirectAttributes redirectAttrs) {

		/*************************** 1.接收請求參數 - 輸入格式的錯誤處理 ************************/
		if (result.hasErrors()) {
			return "back-end/venueReview/update_venueReview_input";
		}

		/*************************** 2.開始修改資料 *****************************************/
		venueReviewSvc.updateVenueReview(venueReviewVO);

		/*************************** 3.修改完成,準備轉交(Send the Success view) **************/
		redirectAttrs.addFlashAttribute("success", "- (修改成功)");
		return "redirect:/venueReview/listAllVenueReview";
	}

	// ===== 刪除 =====
	@PostMapping("delete")
	public String delete(@RequestParam("venueReviewId") Integer venueReviewId, ModelMap model) {

		/*************************** 1.接收請求參數 - 輸入格式的錯誤處理 ************************/
		/*************************** 2.開始刪除資料 *****************************************/
		venueReviewSvc.deleteVenueReview(venueReviewId);

		/*************************** 3.刪除完成,準備轉交(Send the Success view) **************/
		List<VenueReviewVO> list = venueReviewSvc.getAll();
		model.addAttribute("venueReviewListData", list);
		model.addAttribute("success", "- (刪除成功)");
		return "back-end/venueReview/listAllVenueReview";
	}

	// ===== 查詢全部 =====
	@GetMapping("listAllVenueReview")
	public String listAllVenueReview(ModelMap model) {
		List<VenueReviewVO> list = venueReviewSvc.getAll();
		model.addAttribute("venueReviewListData", list);
		return "back-end/venueReview/listAllVenueReview";
	}

	// ===== 查看單筆內容（唯讀） =====
	@GetMapping("viewOne")
	public String viewOne(@RequestParam("venueReviewId") Integer venueReviewId, ModelMap model) {
		VenueReviewVO venueReviewVO = venueReviewSvc.getOneVenueReview(venueReviewId);
		VenueVO venueVO = venueService.getOneVenue(venueReviewVO.getVenueId());		
		model.addAttribute("venueVO", venueVO);
		model.addAttribute("venueReviewVO", venueReviewVO);
		return "back-end/venueReview/listOneVenueReview";
	}

	// ===== 審核通過 =====
	@PostMapping("approve")
	public String approve(@RequestParam("venueReviewId") Integer venueReviewId,
			@RequestParam("employeeId") Integer employeeId,
			@RequestParam(value = "reviewNote", required = false) String reviewNote, RedirectAttributes redirectAttrs) {
		venueReviewSvc.approve(venueReviewId, employeeId, reviewNote);
		redirectAttrs.addFlashAttribute("success", "- (審核通過)");
		return "redirect:/venueReview/listAllVenueReview";
	}

	// ===== 審核未通過 =====
	@PostMapping("reject")
	public String reject(@RequestParam("venueReviewId") Integer venueReviewId,
			@RequestParam("employeeId") Integer employeeId,
			@RequestParam(value = "reviewNote", required = false) String reviewNote, RedirectAttributes redirectAttrs) {
		venueReviewSvc.reject(venueReviewId, employeeId, reviewNote);
		redirectAttrs.addFlashAttribute("success", "- (已標記為審核未通過)");
		return "redirect:/venueReview/listAllVenueReview";
	}

	// ===== 複合查詢：狀態 + 場地編號 + 員工編號，任意組合 =====
	@PostMapping("search")
	public String search(@RequestParam(value = "reviewStatus", required = false) Byte reviewStatus,
			@RequestParam(value = "venueId", required = false) Integer venueId,
			@RequestParam(value = "employeeId", required = false) Integer employeeId, ModelMap model) {

		List<VenueReviewVO> list = venueReviewSvc.getAll();

		if (reviewStatus != null) {
			list = list.stream().filter(vr -> reviewStatus.equals(vr.getReviewStatus())).toList();
		}

		if (venueId != null) {
			list = list.stream().filter(vr -> venueId.equals(vr.getVenueId())).toList();
		}

		if (employeeId != null) {
			list = list.stream().filter(vr -> employeeId.equals(vr.getEmployeeId())).toList();
		}

		model.addAttribute("venueReviewListData", list);
		model.addAttribute("searchReviewStatus", reviewStatus);
		model.addAttribute("searchVenueId", venueId);
		model.addAttribute("searchEmployeeId", employeeId);
		return "back-end/venueReview/listAllVenueReview";
	}

	// ===== 提供員工編號清單 =====
	@ModelAttribute("employeeListData")
	protected List<EmployeeVO> referenceEmployeeList() {
		return employeeRepository.findAll();
	}

	// ===== 提供「員工編號 → 姓名」對照表，給列表頁/單筆顯示頁查詢用 =====
	@ModelAttribute("employeeNameMap")
	protected Map<Integer, String> referenceEmployeeNameMap() {
		return employeeRepository.findAll().stream()
				.collect(Collectors.toMap(EmployeeVO::getEmployeeId, EmployeeVO::getEmpName));
	}
}