package com.webond.platform.controller;

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
import com.webond.platform.model.FaqVO;
import com.webond.platform.service.FaqService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/admin/platform/faq")
public class FaqController {

	@Autowired
	private FaqService faqSvc;

	@Autowired
	private EmployeeRepository employeeRepository;

	// ===== 新增：顯示表單 =====
	@GetMapping("addFaq")
	public String addFaq(ModelMap model) {
		model.addAttribute("faqVO", new FaqVO());
		return "back-end/faq/addFaq";
	}

	// ===== 新增：送出表單 =====
	@PostMapping("insert")
	public String insert(@Valid FaqVO faqVO, BindingResult result, ModelMap model) {

		/*************************** 1.接收請求參數 - 輸入格式的錯誤處理 ************************/
		if (result.hasErrors()) {
			return "back-end/faq/addFaq";
		}

		/*************************** 2.開始新增資料 *****************************************/
		faqSvc.addFaq(faqVO);

		/*************************** 3.新增完成,準備轉交(Send the Success view) **************/
		model.addAttribute("success", "- (新增成功)");
		return "redirect:/faq/listAllFaq";
	}

	// ===== 查詢單筆（供修改用） =====
	@PostMapping("getOne_For_Update")
	public String getOneForUpdate(@RequestParam("faqId") Integer faqId, ModelMap model) {
		/*************************** 1.接收請求參數 - 輸入格式的錯誤處理 ************************/
		/*************************** 2.開始查詢資料 *****************************************/
		FaqVO faqVO = faqSvc.getOneFaq(faqId);

		/*************************** 3.查詢完成,準備轉交(Send the Success view) **************/
		model.addAttribute("faqVO", faqVO);
		return "back-end/faq/update_faq_input"; // 查詢完成後轉交update_faq_input.html
	}

	// ===== 修改 =====
	@PostMapping("update")
	public String update(@Valid FaqVO faqVO, BindingResult result, ModelMap model, RedirectAttributes redirectAttrs) {

		/*************************** 1.接收請求參數 - 輸入格式的錯誤處理 ************************/
		if (result.hasErrors()) {
			return "back-end/faq/update_faq_input";
		}

		/*************************** 2.開始修改資料 *****************************************/
		faqSvc.updateFaq(faqVO);

		/*************************** 3.修改完成,準備轉交(Send the Success view) **************/
		// 用 FlashAttribute 保存訊息，確保 redirect 後列表頁仍能用 ${success} 抓到它
		redirectAttrs.addFlashAttribute("success", "- (修改成功)");
		// 安全地重導向回列表頁，徹底解決上一頁 404 的問題
		return "redirect:/faq/listAllFaq";
	}

	// ===== 刪除 =====
	@PostMapping("delete")
	public String delete(@RequestParam("faqId") Integer faqId, ModelMap model) {

		/*************************** 1.接收請求參數 - 輸入格式的錯誤處理 ************************/
		/*************************** 2.開始刪除資料 *****************************************/
		faqSvc.deleteFaq(faqId);

		/*************************** 3.刪除完成,準備轉交(Send the Success view) **************/
		List<FaqVO> list = faqSvc.getAll();
		model.addAttribute("faqListData", list);
		model.addAttribute("success", "- (刪除成功)");
		return "back-end/faq/listAllFaq"; // 刪除完成後轉交listAllFaq.html
	}

	// ===== 查詢全部 =====
	@GetMapping("listAllFaq")
	public String listAllFaq(ModelMap model, HttpSession session) {

		EmployeeVO loginEmp = (EmployeeVO) session.getAttribute("employeeVO");
		if (loginEmp == null) {
			return "redirect:/admin/login";
		}

		List<FaqVO> list = faqSvc.getAll();
		model.addAttribute("faqListData", list);
		return "back-end/faq/listAllFaq";
	}

	// ===== 查看單筆內容（唯讀） =====
	@GetMapping("viewOne")
	public String viewOne(@RequestParam("faqId") Integer faqId, ModelMap model) {
		FaqVO faqVO = faqSvc.getOneFaq(faqId);
		model.addAttribute("faqVO", faqVO);
		return "back-end/faq/listOneFaq";
	}

	// ===== 發布 =====
	@PostMapping("publish")
	public String publish(@RequestParam("faqId") Integer faqId, RedirectAttributes redirectAttrs) {
		faqSvc.publish(faqId);
		redirectAttrs.addFlashAttribute("success", "- (發布成功)");
		return "redirect:/faq/listAllFaq";
	}

	// ===== 複合查詢：狀態 + FAQ類型 + 問題關鍵字，任意組合 =====
	@PostMapping("search")
	public String search(@RequestParam(value = "status", required = false) Byte status,
			@RequestParam(value = "faqType", required = false) Byte faqType,
			@RequestParam(value = "keyword", required = false) String keyword, ModelMap model) {

		boolean hasKeyword = keyword != null && !keyword.isBlank();

		List<FaqVO> list = faqSvc.getAll();

		if (status != null) {
			list = list.stream().filter(faq -> status.equals(faq.getStatus())).toList();
		}

		if (faqType != null) {
			list = list.stream().filter(faq -> faqType.equals(faq.getFaqType())).toList();
		}

		if (hasKeyword) {
			list = list.stream().filter(faq -> (faq.getQuestion() != null && faq.getQuestion().contains(keyword))
					|| (faq.getAnswer() != null && faq.getAnswer().contains(keyword))).toList();
		}

		model.addAttribute("faqListData", list);
		model.addAttribute("searchStatus", status);
		model.addAttribute("searchFaqType", faqType);
		model.addAttribute("searchKeyword", keyword);
		return "back-end/faq/listAllFaq";
	}

	// ===== 提供FAQ類型清單給下拉選單使用 =====
	@ModelAttribute("faqTypeListData")
	protected Map<Byte, String> referenceFaqTypeList() {
		return FaqService.FAQ_TYPE_LABELS;
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