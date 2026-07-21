package com.webond.platform.controller;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URLConnection;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.webond.employee.model.EmployeeVO;
import com.webond.employee.repository.EmployeeRepository;
import com.webond.platform.model.BulletinVO;
import com.webond.platform.service.BulletinService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/admin/platform/bulletin")
public class BulletinController {

	@Autowired
	private BulletinService bulletinSvc;

	@Autowired
	private EmployeeRepository employeeRepository;

	/*
	 * This method will serve as addBulletin.html handler.
	 */
	@GetMapping("addBulletin")
	public String addBulletin(ModelMap model, HttpSession session) {

		EmployeeVO loginEmp = (EmployeeVO) session.getAttribute("employeeVO");
		if (loginEmp == null) {
			return "redirect:/admin/login";
		}

		BulletinVO bulletinVO = new BulletinVO();
		bulletinVO.setEmployeeId(loginEmp.getEmployeeId());
		model.addAttribute("bulletinVO", bulletinVO);
		model.addAttribute("loginEmp", loginEmp);
		return "back-end/bulletin/addBulletin";
	}

	/*
	 * This method will be called on addBulletin.html form submission, handling POST
	 * request It also validates the user input
	 */
	@PostMapping("insert")
	public String insert(@Valid BulletinVO bulletinVO, BindingResult result, ModelMap model, HttpSession session,
			@RequestParam(value = "upImg", required = false) MultipartFile upImg) throws IOException {

		EmployeeVO loginEmp = (EmployeeVO) session.getAttribute("employeeVO");
		if (loginEmp == null) {
			return "redirect:/admin/login";
		}

		// 負責員工一律代入目前登入的員工，不開放手動選擇
		bulletinVO.setEmployeeId(loginEmp.getEmployeeId());

		/*************************** 1.接收請求參數 - 輸入格式的錯誤處理 ************************/
		if (result.hasErrors()) {
			model.addAttribute("loginEmp", loginEmp);
			return "back-end/bulletin/addBulletin";
		}

		if (upImg != null && !upImg.isEmpty()) {
			bulletinVO.setImage(upImg.getBytes());
		}

		/*************************** 2.開始新增資料 *****************************************/
		bulletinSvc.addBulletin(bulletinVO);

		/*************************** 3.新增完成,準備轉交(Send the Success view) **************/
		model.addAttribute("success", "- (新增成功)");
		return "redirect:/admin/platform/bulletin/listAllBulletin";
	}

	/*
	 * This method will be called on listAllBulletin.html form submission, handling
	 * POST request
	 */
	@PostMapping("getOne_For_Update")
	public String getOne_For_Update(@RequestParam("bulletinId") String bulletinId, ModelMap model,
			HttpSession session) {

		EmployeeVO loginEmp = (EmployeeVO) session.getAttribute("employeeVO");
		if (loginEmp == null) {
			return "redirect:/admin/login";
		}

		/*************************** 1.接收請求參數 - 輸入格式的錯誤處理 ************************/
		/*************************** 2.開始查詢資料 *****************************************/
		BulletinVO bulletinVO = bulletinSvc.getOneBulletin(Integer.valueOf(bulletinId));
		// 負責員工一律代入目前登入的員工，不開放手動選擇
		bulletinVO.setEmployeeId(loginEmp.getEmployeeId());

		/*************************** 3.查詢完成,準備轉交(Send the Success view) **************/
		model.addAttribute("bulletinVO", bulletinVO);
		model.addAttribute("loginEmp", loginEmp);
		return "back-end/bulletin/update_bulletin_input"; // 查詢完成後轉交update_bulletin_input.html
	}

	/*
	 * This method will be called on update_emp_input.html form submission, handling
	 * POST request It also validates the user input
	 */
	// 方法簽章尾端加入 RedirectAttributes redirectAttrs
	@PostMapping("update")
	public String update(@Valid BulletinVO bulletinVO, BindingResult result, ModelMap model, HttpSession session,
			RedirectAttributes redirectAttrs,
			@RequestParam(value = "upImg", required = false) MultipartFile upImg) throws IOException {

		EmployeeVO loginEmp = (EmployeeVO) session.getAttribute("employeeVO");
		if (loginEmp == null) {
			return "redirect:/admin/login";
		}

		// 負責員工一律代入目前登入的員工，不開放手動選擇
		bulletinVO.setEmployeeId(loginEmp.getEmployeeId());

		/*************************** 1.接收請求參數 - 輸入格式的錯誤處理 ************************/
		if (result.hasErrors()) {
			model.addAttribute("loginEmp", loginEmp);
			return "back-end/bulletin/update_bulletin_input";
		}

		if (upImg != null && !upImg.isEmpty()) {
			bulletinVO.setImage(upImg.getBytes());
		}

		/*************************** 2.開始修改資料 *****************************************/
		bulletinSvc.updateBulletin(bulletinVO);

		/*************************** 3.修改完成,準備轉交(Send the Success view) **************/
		// ★ 用 redirect，避免同一個 Session 的一級快取回傳被 merge 覆蓋成 null 的
		// createdAt / updatedAt；redirect 後重新查詢才會拿到 DB 的最新時間
		redirectAttrs.addFlashAttribute("success", "- (修改成功)");
		return "redirect:/admin/platform/bulletin/viewOne?bulletinId=" + bulletinVO.getBulletinId();
	}

	/*
	 * This method will be called on listAllEmp.html form submission, handling POST
	 * request
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
	public String listAllBulletin(ModelMap model, HttpSession session) {

		EmployeeVO loginEmp = (EmployeeVO) session.getAttribute("employeeVO");
		if (loginEmp == null) {
			return "redirect:/admin/login";
		}

		List<BulletinVO> list = bulletinSvc.getAll();
		model.addAttribute("bulletinListData", list);
		return "back-end/bulletin/listAllBulletin";
	}

	// ===== 發布 =====
	@PostMapping("publish")
	public String publish(@RequestParam("bulletinId") Integer bulletinId, RedirectAttributes redirectAttrs) {
		bulletinSvc.publish(bulletinId);
		redirectAttrs.addFlashAttribute("success", "- (發布成功)");
		return "redirect:/admin/platform/bulletin/listAllBulletin";
	}

	// ===== 複合查詢：狀態 + （標題或標籤關鍵字）+ 發布日期區間，任意組合 =====
	@PostMapping("search")
	public String search(@RequestParam(value = "status", required = false) Byte status,
			@RequestParam(value = "keyword", required = false) String keyword,
			@RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
			@RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
			ModelMap model) {

		boolean hasKeyword = keyword != null && !keyword.isBlank();
		boolean hasDateRange = startDate != null && endDate != null;

		// 發布日期區間只填了一端：不執行日期篩選，並提示使用者補填完整區間
		if ((startDate != null) != (endDate != null)) {
			model.addAttribute("dateRangeError", "請完整填寫發布日期區間的起訖日期");
		}

		// 1. 先用「範圍最明確」的條件從資料庫撈出基礎清單
		List<BulletinVO> list = hasDateRange ? bulletinSvc.getByPublishDateBetween(startDate, endDate)
				: bulletinSvc.getAll();

		// 2. 依狀態進一步篩選
		if (status != null) {
			list = list.stream().filter(bulletin -> status.equals(bulletin.getStatus())).toList();
		}

		// 3. 依關鍵字篩選：標題「或」標籤有包含到即算符合
		if (hasKeyword) {
			list = list.stream()
					.filter(bulletin -> (bulletin.getTitle() != null && bulletin.getTitle().contains(keyword))
							|| (bulletin.getTags() != null && bulletin.getTags().contains(keyword))
							|| (bulletin.getContent() != null && bulletin.getContent().contains(keyword)))
					.toList();
		}

		model.addAttribute("bulletinListData", list);

		// 搜尋條件也放回 model，讓表單能回填顯示
		model.addAttribute("searchStatus", status);
		model.addAttribute("searchKeyword", keyword);
		model.addAttribute("searchStartDate", startDate);
		model.addAttribute("searchEndDate", endDate);
		return "back-end/bulletin/listAllBulletin";
	}

	// ===== 查看單筆內容（唯讀，不可修改） =====
	// 改為同時支援 GET/POST （列表頁的 form 走 POST，update 成功後的 redirect 走 GET）
	@RequestMapping(value = "viewOne", method = { RequestMethod.GET, RequestMethod.POST })
	public String viewOne(@RequestParam("bulletinId") Integer bulletinId, ModelMap model) {
		BulletinVO bulletinVO = bulletinSvc.getOneBulletin(bulletinId);
		model.addAttribute("bulletinVO", bulletinVO);
		return "back-end/bulletin/listOneBulletin";
	}

	// ===== 提供「員工編號 → 姓名」對照表，給列表頁/單筆顯示頁查詢用 =====
	@ModelAttribute("employeeNameMap")
	protected Map<Integer, String> referenceEmployeeNameMap() {
		return employeeRepository.findAll().stream()
				.collect(Collectors.toMap(EmployeeVO::getEmployeeId, EmployeeVO::getEmpName));
	}

	// ===== 後台：取得公告圖片（草稿/已發布皆可查看，供管理者預覽用） =====
	@GetMapping("image/{bulletinId}")
	@ResponseBody
	public ResponseEntity<byte[]> getImage(@PathVariable Integer bulletinId) {

		BulletinVO bulletinVO = bulletinSvc.getOneBulletin(bulletinId);
		if (bulletinVO == null || bulletinVO.getImage() == null || bulletinVO.getImage().length == 0) {
			return ResponseEntity.notFound().build();
		}

		byte[] imgBytes = bulletinVO.getImage();
		String imgType = "image/jpeg";

		try {
			imgType = URLConnection
					.guessContentTypeFromStream(new BufferedInputStream(new ByteArrayInputStream(imgBytes)));
			if (imgType == null) {
				imgType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
			}
		} catch (Exception e) {
			// 圖片格式偵測失敗，維持預設 image/jpeg
		}

		return ResponseEntity.ok()
				.contentType(MediaType.parseMediaType(imgType))
				.header("Cache-Control", "max-age=3600")
				.body(imgBytes);
	}
}