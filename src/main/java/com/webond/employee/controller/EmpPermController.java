package com.webond.employee.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.webond.employee.model.EmpPermService;
import com.webond.employee.model.PermissionService;
import com.webond.employee.model.PermissionVO;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/admin/employees")
public class EmpPermController {

	@Autowired
	PermissionService permSvc;

	@Autowired
	EmpPermService empPermSvc;

// === 查詢所有權限 === //	
	@GetMapping("/empPermPage")
	public String getEmpPermList(@RequestParam(value = "searchPermName", required = false) String searchPermName,
			@RequestParam(value = "searchHasEmp", required = false) String searchHasEmp,
			@RequestParam(value = "p", defaultValue = "0") Integer p, Model model,
			@RequestParam(value = "openAddModal", required = false) Boolean openAddModal,
			@RequestParam(value = "openEditModal", required = false) Boolean openEditModal) {

		Page<PermissionVO> permPageData = permSvc.getAllPermsByPage(searchPermName, searchHasEmp, p);
		model.addAttribute("permListData", permPageData.getContent());
		model.addAttribute("currentPage", p);
		model.addAttribute("totalPages", permPageData.getTotalPages());

		model.addAttribute("currentPermName", searchPermName);
		model.addAttribute("currentHasEmp", searchHasEmp);

		model.addAttribute("openAddModal", openAddModal);
		model.addAttribute("openEditModal", openEditModal);

		if (!model.containsAttribute("permissionVO")) {
			model.addAttribute("permissionVO", new PermissionVO());
		}

		if (!model.containsAttribute("addPermissionVO")) {
			model.addAttribute("addPermissionVO", new PermissionVO());
		}

		return "back-end/permission/empPermPage";
	}

// === 新增燈箱畫面 ===//
	@GetMapping("/empPermPage/addModal")
	public String getAddModal(Model model) {

		if (!model.containsAttribute("addPermissionVO")) {
			model.addAttribute("addPermissionVO", new PermissionVO());
		}

		return "back-end/permission/addPerm :: addPermContent";
	}

// === 新增燈箱 (確認送出資料) === //
	@PostMapping("/addPerm")
	public String insert(@Valid PermissionVO permVO, BindingResult result, ModelMap model,
			@RequestParam(value = "currentPage", defaultValue = "0") int currentPage,
			@RequestParam(value = "searchPermName", required = false) String searchPermName,
			@RequestParam(value = "searchHasEmp", required = false) String searchHasEmp,
			RedirectAttributes redirectAttributes) {

		if (result.hasErrors()) {
			redirectAttributes.addFlashAttribute(BindingResult.MODEL_KEY_PREFIX + "addPermissionVO", result);
			redirectAttributes.addFlashAttribute("addPermissionVO", permVO);
			redirectAttributes.addFlashAttribute("openAddModal", true);

			StringBuilder errorUrl = new StringBuilder("redirect:/admin/employees/empPermPage?p=" + currentPage);
			errorUrl.append("&openAddModal=true");
			if (searchPermName != null && !searchPermName.trim().isEmpty())
				errorUrl.append("&searchPermName=").append(searchPermName.trim());
			if (searchHasEmp != null && !searchHasEmp.trim().isEmpty())
				errorUrl.append("&searchHasEmp=").append(searchHasEmp);

			return errorUrl.toString();
		}

		permSvc.addPermission(permVO);
		redirectAttributes.addFlashAttribute("successMessage", "🎉 權限資料新增成功！");

		StringBuilder successUrl = new StringBuilder("redirect:/admin/employees/empPermPage?p=" + currentPage);
		if (searchPermName != null && !searchPermName.trim().isEmpty())
			successUrl.append("&searchPermName=").append(searchPermName.trim());
		if (searchHasEmp != null && !searchHasEmp.trim().isEmpty())
			successUrl.append("&searchHasEmp=").append(searchHasEmp);

		return successUrl.toString();
	}

// === 修改燈箱畫面 ===//
	@GetMapping("/empPermPage/editModal")
	public String getEditModal(Model model) {

		if (!model.containsAttribute("permissionVO")) {
			model.addAttribute("permissionVO", new PermissionVO());
		}

		return "back-end/permission/editPerm :: editPermContent";
	}

// === 修改燈箱 (確認送出資料) === //	
	@PostMapping("/updatePerm")
	public String update(@Valid PermissionVO permVO, BindingResult result, ModelMap model,
			@RequestParam(value = "currentPage", defaultValue = "0") int currentPage,
			@RequestParam(value = "searchPermName", required = false) String searchPermName,
			@RequestParam(value = "searchHasEmp", required = false) String searchHasEmp,
			RedirectAttributes redirectAttributes) {

		if (result.hasErrors()) {
			redirectAttributes.addFlashAttribute(BindingResult.MODEL_KEY_PREFIX + "permissionVO", result);
			redirectAttributes.addFlashAttribute("permissionVO", permVO);
			redirectAttributes.addFlashAttribute("openEditModal", true);

			StringBuilder errorUrl = new StringBuilder("redirect:/admin/employees/empPermPage?p=" + currentPage);
			errorUrl.append("&openEditModal=true");
			if (searchPermName != null && !searchPermName.trim().isEmpty())
				errorUrl.append("&searchPermName=").append(searchPermName.trim());
			if (searchHasEmp != null && !searchHasEmp.trim().isEmpty())
				errorUrl.append("&searchHasEmp=").append(searchHasEmp);

			return errorUrl.toString();
		}

		permSvc.updatePerm(permVO);
		redirectAttributes.addFlashAttribute("successMessage", "🎉 權限資料修改成功！");

		StringBuilder successUrl = new StringBuilder("redirect:/admin/employees/empPermPage?p=" + currentPage);
		if (searchPermName != null && !searchPermName.trim().isEmpty())
			successUrl.append("&searchPermName=").append(searchPermName.trim());
		if (searchHasEmp != null && !searchHasEmp.trim().isEmpty())
			successUrl.append("&searchHasEmp=").append(searchHasEmp);

		return successUrl.toString();
	}

// === 刪除員工權限(確認送出) ===//
	@PostMapping("/permDelete")
	public String deletePerm(@RequestParam("permId") Integer permId,
			@RequestParam(value = "currentPage", defaultValue = "0") int currentPage,
			@RequestParam(value = "searchPermName", required = false) String searchPermName, 
	        @RequestParam(value = "searchHasEmp", required = false) String searchHasEmp,
	        RedirectAttributes redirectAttributes) {
		/*************************** 1.接收請求參數 - 輸入格式的錯誤處理 ************************/
		/*************************** 2.開始刪除資料 *****************************************/
		permSvc.deletePerm(permId);
		/*************************** 3.刪除完成,準備轉交(Send the Success view) **************/
		redirectAttributes.addFlashAttribute("successMessage", "🗑️ 權限已成功從系統精確抹除！");
	    
	    StringBuilder redirectUrl = new StringBuilder("redirect:/admin/employees/empPermPage?p=" + currentPage);
	    if (searchPermName != null && !searchPermName.trim().isEmpty()) redirectUrl.append("&searchPermName=").append(searchPermName.trim());
	    if (searchHasEmp != null && !searchHasEmp.trim().isEmpty()) redirectUrl.append("&searchHasEmp=").append(searchHasEmp);
	    
	    return redirectUrl.toString();

	}

}
