package com.webond.platform.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.webond.platform.model.BulletinVO;
import com.webond.platform.service.BulletinService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/bulletin")
public class BulletinController {

	@Autowired
	private BulletinService bulletinSvc;
	
	/*
	 * This method will serve as addBulletin.html handler.
	 */
	@GetMapping("addBulletin")
	public String addBulletin(ModelMap model) {
		BulletinVO bulletinVO = new BulletinVO();
		model.addAttribute("bulletinVO", bulletinVO);
		return "back-end/bulletin/addBulletin";
	}
	
	/*
	 * This method will be called on addBulletin.html form submission, handling POST request It also validates the user input
	 */
	@PostMapping("insert")
	public String insert(@Valid BulletinVO bulletinVO, BindingResult result, ModelMap model) {
	
		/*************************** 1.接收請求參數 - 輸入格式的錯誤處理 ************************/
		if (result.hasErrors()) {
			return "back-end/bulletin/addBulletin";
		}
		
		/*************************** 2.開始新增資料 *****************************************/
		bulletinSvc.addBulletin(bulletinVO);
		
		/*************************** 3.新增完成,準備轉交(Send the Success view) **************/
		model.addAttribute("success", "- (新增成功)");
		return "redirect:/bulletin/listAllBulletin";
	}
	
	/*
	 * This method will be called on listAllBulletin.html form submission, handling POST request
	 */
	@PostMapping("getOne_For_Update")
	public String getOne_For_Update(@RequestParam("bulletinId") String bulletinId, ModelMap model) {
		
		/*************************** 1.接收請求參數 - 輸入格式的錯誤處理 ************************/
		/*************************** 2.開始查詢資料 *****************************************/
		BulletinVO bulletinVO = bulletinSvc.getOneBulletin(Integer.valueOf(bulletinId));
		
		/*************************** 3.查詢完成,準備轉交(Send the Success view) **************/
		model.addAttribute("bulletinVO", bulletinVO);
		return "back-end/bulletin/update_bulletin_input"; // 查詢完成後轉交update_bulletin_input.html
	}
	
	/*
	 * This method will be called on update_emp_input.html form submission, handling POST request It also validates the user input
	 */
	@PostMapping("update")
	public String update(@Valid BulletinVO bulletinVO, BindingResult result, ModelMap model) {
		
		/*************************** 1.接收請求參數 - 輸入格式的錯誤處理 ************************/
		if (result.hasErrors()) {
			return "back-end/bulletin/update_bulletin_input";
		}
		
		/*************************** 2.開始修改資料 *****************************************/
		bulletinSvc.updateBulletin(bulletinVO);

		/*************************** 3.修改完成,準備轉交(Send the Success view) **************/
		model.addAttribute("success", "- (修改成功)");
		bulletinVO = bulletinSvc.getOneBulletin(bulletinVO.getBulletinId());
		model.addAttribute("bulletinVO", bulletinVO);
		return "back-end/bulletin/listOneBulletin"; // 修改成功後轉交listOneBulletin.html
	}
	
	/*
	 * This method will be called on listAllEmp.html form submission, handling POST request
	 */
	@PostMapping("delete")
	public String delete(@RequestParam("bulletinId") Integer bulletinId, ModelMap model) {
		/*************************** 1.接收請求參數 - 輸入格式的錯誤處理 ************************/
		/*************************** 2.開始刪除資料 *****************************************/
		bulletinSvc.deleteBulletin(bulletinId);
		
		/*************************** 3.刪除完成,準備轉交(Send the Success view) **************/
		List<BulletinVO> list = bulletinSvc.getAll();
		model.addAttribute("bulletinListData", list);
		model.addAttribute("success", "- (刪除成功)");
		return "back-end/bulletin/listAllBulletin"; // 刪除完成後轉交listAllBulletin.html
	}
	
	// ===== 查詢全部 =====
	@GetMapping("listAllBulletin")
	public String listAllBulletin(ModelMap model) {
		List<BulletinVO> list = bulletinSvc.getAll();
		model.addAttribute("bulletinListData", list);
		return "back-end/bulletin/listAllBulletin";
	}

	// ===== 發布 =====
	@PostMapping("publish")
	public String publish(@RequestParam("bulletinId") Integer bulletinId, RedirectAttributes redirectAttrs) {
		bulletinSvc.publish(bulletinId);
		redirectAttrs.addFlashAttribute("success", "- (發布成功)");
		return "redirect:/bulletin/listAllBulletin";
	}

	// ===== 收回為草稿 =====
	@PostMapping("unpublish")
	public String unpublish(@RequestParam("bulletinId") Integer bulletinId, RedirectAttributes redirectAttrs) {
		bulletinSvc.unpublish(bulletinId);
		redirectAttrs.addFlashAttribute("success", "- (已收回為草稿)");
		return "redirect:/bulletin/listAllBulletin";
	}

	// ===== 複合條件查詢 =====
	@PostMapping("search")
	public String search(
			@RequestParam(value = "status", required = false) Byte status,
			@RequestParam(value = "keyword", required = false) String keyword,
			ModelMap model) {

		List<BulletinVO> list;
		if (status != null && keyword != null && !keyword.isBlank()) {
			list = bulletinSvc.getByStatusAndTitleLike(status, keyword);
		} else if (status != null) {
			list = bulletinSvc.getByStatus(status);
		} else if (keyword != null && !keyword.isBlank()) {
			list = bulletinSvc.getByTitleLike(keyword);
		} else {
			list = bulletinSvc.getAll();
		}

		model.addAttribute("bulletinListData", list);
		return "back-end/bulletin/listAllBulletin";
	}

	// ===== 依日期區間查詢 =====
	@PostMapping("searchByDateRange")
	public String searchByDateRange(
	        @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
	        @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
	        ModelMap model) {

	    List<BulletinVO> list = bulletinSvc.getByPublishDateBetween(startDate, endDate);
	    model.addAttribute("bulletinListData", list);
	    return "back-end/bulletin/listAllBulletin";
	}
}
