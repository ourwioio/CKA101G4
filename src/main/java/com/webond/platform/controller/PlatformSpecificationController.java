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
import com.webond.platform.model.PlatformSpecificationVO;
import com.webond.platform.service.PlatformSpecificationService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/platformSpecification")
public class PlatformSpecificationController {

	@Autowired
	private PlatformSpecificationService specSvc;
	
    @Autowired
    private EmployeeRepository employeeRepository;

	// ===== 新增：顯示表單 =====
	@GetMapping("addSpec")
	public String addSpec(ModelMap model) {
		model.addAttribute("specVO", new PlatformSpecificationVO());
		return "back-end/platformSpecification/addSpec";
	}

	// ===== 新增：送出表單 =====
	@PostMapping("insert")
	public String insert(@Valid PlatformSpecificationVO specVO, BindingResult result, ModelMap model) {

		/*************************** 1.接收請求參數 - 輸入格式的錯誤處理 ************************/
		if (result.hasErrors()) {
			return "back-end/platformSpecification/addSpec";
		}

		/*************************** 2.開始新增資料 *****************************************/
		specSvc.addSpec(specVO);

		/*************************** 3.新增完成,準備轉交(Send the Success view) **************/
		model.addAttribute("success", "- (新增成功)");
		return "redirect:/platformSpecification/listAllSpec";
	}

	// ===== 查詢單筆（供修改用） =====
	@PostMapping("getOne_For_Update")
	public String getOneForUpdate(@RequestParam("specId") Integer specId, ModelMap model) {
		/*************************** 1.接收請求參數 - 輸入格式的錯誤處理 ************************/
		/*************************** 2.開始查詢資料 *****************************************/
		PlatformSpecificationVO specVO = specSvc.getOneSpec(specId);
		
		/*************************** 3.查詢完成,準備轉交(Send the Success view) **************/
		model.addAttribute("specVO", specVO);
		return "back-end/platformSpecification/update_spec_input"; // 查詢完成後轉交update_spec_input.html
	}

	// ===== 修改 =====
	@PostMapping("update")
	public String update(@Valid PlatformSpecificationVO specVO, BindingResult result, ModelMap model, RedirectAttributes redirectAttrs) {

		/*************************** 1.接收請求參數 - 輸入格式的錯誤處理 ************************/
		if (result.hasErrors()) {
			return "back-end/platformSpecification/update_spec_input";
		}

		/*************************** 2.開始修改資料 *****************************************/
		specSvc.updateSpec(specVO);
		
		/*************************** 3.修改完成,準備轉交(Send the Success view) **************/
		// 用 FlashAttribute 保存訊息，確保 redirect 後列表頁仍能用 ${success} 抓到它
	    redirectAttrs.addFlashAttribute("success", "- (修改成功)");
	 // 安全地重導向回列表頁，徹底解決上一頁 404 的問題
		return "redirect:/platformSpecification/listAllSpec";
	}

	// ===== 刪除 =====
	@PostMapping("delete")
	public String delete(@RequestParam("specId") Integer specId, ModelMap model) {
		
		/*************************** 1.接收請求參數 - 輸入格式的錯誤處理 ************************/
		/*************************** 2.開始刪除資料 *****************************************/
		specSvc.deleteSpec(specId);
		
		/*************************** 3.刪除完成,準備轉交(Send the Success view) **************/
		List<PlatformSpecificationVO> list = specSvc.getAll();
		model.addAttribute("specListData", list);
		model.addAttribute("success", "- (刪除成功)");
		return "back-end/platformSpecification/listAllSpec"; // 刪除完成後轉交listAllSpec.html
	}

	// ===== 查詢全部 =====
	@GetMapping("listAllSpec")
	public String listAllSpec(ModelMap model) {
		List<PlatformSpecificationVO> list = specSvc.getAll();
		model.addAttribute("specListData", list);
		return "back-end/platformSpecification/listAllSpec";
	}

	// ===== 查看單筆內容（唯讀） =====
	@GetMapping("viewOne")
	public String viewOne(@RequestParam("specId") Integer specId, ModelMap model) {
		PlatformSpecificationVO specVO = specSvc.getOneSpec(specId);
		model.addAttribute("specVO", specVO);
		return "back-end/platformSpecification/listOneSpec";
	}

	// ===== 發布 =====
	@PostMapping("publish")
	public String publish(@RequestParam("specId") Integer specId, RedirectAttributes redirectAttrs) {
		specSvc.publish(specId);
		redirectAttrs.addFlashAttribute("success", "- (發布成功)");
		return "redirect:/platformSpecification/listAllSpec";
	}

	// ===== 複合查詢：狀態 + 規範類型 + 標題關鍵字，任意組合 =====
	@PostMapping("search")
	public String search(
			@RequestParam(value = "status", required = false) Byte status,
			@RequestParam(value = "specType", required = false) Byte specType,
			@RequestParam(value = "keyword", required = false) String keyword,
			ModelMap model) {

		boolean hasKeyword = keyword != null && !keyword.isBlank();

		List<PlatformSpecificationVO> list = specSvc.getAll();

		if (status != null) {
			list = list.stream()
					.filter(spec -> status.equals(spec.getStatus()))
					.toList();
		}

		if (specType != null) {
			list = list.stream()
					.filter(spec -> specType.equals(spec.getSpecType()))
					.toList();
		}

		if (hasKeyword) {
			list = list.stream()
					.filter(spec -> spec.getTitle() != null && spec.getTitle().contains(keyword))
					.toList();
		}

		model.addAttribute("specListData", list);
		model.addAttribute("searchStatus", status);
		model.addAttribute("searchSpecType", specType);
		model.addAttribute("searchKeyword", keyword);
		return "back-end/platformSpecification/listAllSpec";
	}

	// ===== 提供規範類型清單給下拉選單使用 =====
	@ModelAttribute("specTypeListData")
	protected Map<Byte, String> referenceSpecTypeList() {
		return PlatformSpecificationService.SPEC_TYPE_LABELS;
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
