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

import com.webond.employee.model.EmpPermService;
import com.webond.employee.model.EmployeeVO;
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
	public String getEmpPermList(
			@RequestParam(value = "p", defaultValue = "0") Integer p, Model model,
			@RequestParam(value = "openAddModal", required = false) Boolean openAddModal,
			@RequestParam(value = "openEditModal", required = false) Boolean openEditModal){
		
		Page<PermissionVO> permPage = permSvc.getAllByPage(p);
		model.addAttribute("permListData", permPage.getContent());
		model.addAttribute("currentPage", p);
		model.addAttribute("totalPages", permPage.getTotalPages());
		
		model.addAttribute("openAddModal", openAddModal);
		model.addAttribute("openEditModal", openEditModal);
		
		if(!model.containsAttribute("permissionVO")) {
			model.addAttribute("permissionVO", new PermissionVO());
		}
		
		return "back-end/permission/empPermPage";
	}
	
// === 新增燈箱畫面 ===//
	@GetMapping("/empPermPage/addModal")
	public String getAddModal(Model model) {

		if(!model.containsAttribute("permissionVO")) {
			model.addAttribute("permissionVO", new PermissionVO());
		}
		
		return "back-end/permission/addPerm :: addPermContent";
	}
	
	
// === 新增燈箱 (確認送出資料) === //
	@PostMapping("/addPerm")
	public String insert(
			@Valid PermissionVO permVO,
			BindingResult result,
			ModelMap model,
			@RequestParam(value = "currentPage", defaultValue = "0") int currentPage){
		
		if(result.hasErrors()) {
			model.addAttribute("permissionVO",permVO);
			
			Page<PermissionVO> permPage = permSvc.getAllByPage(currentPage);
			model.addAttribute("permListData", permPage.getContent());
			model.addAttribute("currentPage",currentPage);
			model.addAttribute("totalPages", permPage.getTotalPages());

			model.addAttribute("openAddModal", true);
			model.addAttribute("openEditModal", false);
			
			return "back-end/permission/empPermPage";
		}
		
		permSvc.addPermission(permVO);
		
		return "redirect:/admin/employees/empPermPage?p=" + currentPage;
	}
	
	
	
	
// === 修改燈箱畫面 ===//
		@GetMapping("/empPermPage/editModal")
		public String getEditModal(Model model) {

			if(!model.containsAttribute("permissionVO")) {
				model.addAttribute("permissionVO", new PermissionVO());
			}
			
			return "back-end/permission/editPerm :: editPermContent";
		}
	
// === 修改燈箱 (確認送出資料) === //	
		@PostMapping("/updatePerm")
		public String update(
				@Valid PermissionVO permVO,
				BindingResult result,
				ModelMap model,
				@RequestParam(value = "currentPage", defaultValue = "0") int currentPage){
			
			if(result.hasErrors()) {
				model.addAttribute("permissionVO",permVO);
				
				Page<PermissionVO> permPage = permSvc.getAllByPage(currentPage);
				model.addAttribute("permListData", permPage.getContent());
				model.addAttribute("currentPage", currentPage);
				model.addAttribute("totalPages", permPage.getTotalPages());

				model.addAttribute("openEditModal", true);
				model.addAttribute("openAddModal", false);
				
				return "back-end/permission/empPermPage";
			}
			
			permSvc.updatePerm(permVO);
			
			return "redirect:/admin/employees/empPermPage?p=" + currentPage;
		}
		
// === 刪除員工權限(確認送出) ===//
		@PostMapping("/permDelete")
		public String deletePerm(
				@RequestParam("permId") Integer permId,
				ModelMap model){
			/*************************** 1.接收請求參數 - 輸入格式的錯誤處理 ************************/
			/*************************** 2.開始刪除資料 *****************************************/
			permSvc.deletePerm(permId);
			/*************************** 3.刪除完成,準備轉交(Send the Success view) **************/
			List<PermissionVO> list = permSvc.getAll();
			model.addAttribute("permListData", list); 
			model.addAttribute("success", "- (刪除成功)");
			
			return "redirect:/admin/employees/empPermPage";
			
		}
	
	
	


}
