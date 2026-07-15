package com.webond.service.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.webond.employee.model.EmployeeVO;
import com.webond.service.model.ServiceTypeVO;
import com.webond.service.service.ServiceTypeService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping(AdminServiceTypeController.BASE_PATH)
public class AdminServiceTypeController {

	// =========================================================
	// 路徑設定
	// =========================================================

	public static final String BASE_PATH = "/admin/service/types";

	// =========================================================
	// Session 設定
	// =========================================================

	private static final String EMPLOYEE_SESSION_KEY = "employeeVO";

	private static final int DEFAULT_FAKE_EMPLOYEE_ID = 1001;

	private final ServiceTypeService serviceTypeSvc;

	public AdminServiceTypeController(ServiceTypeService serviceTypeSvc) {

		this.serviceTypeSvc = serviceTypeSvc;
	}

	// =========================================================
	// 服務類型管理頁
	//
	// GET /admin/service/types
	// =========================================================

	// =========================================================
	// 服務類型管理及查詢
	//
	// GET /admin/service/types
	// GET /admin/service/types?keyword=陪伴
	// =========================================================

	@GetMapping
	public String listServiceTypes(@RequestParam(name = "keyword", required = false) String keyword,
			HttpSession session, Model model) {

		Integer loginEmployeeId = getLoginEmployeeId(session);

		if (loginEmployeeId == null) {
			return "redirect:" + BASE_PATH + "/fake-login";
		}

		String normalizedKeyword = normalizeNullableText(keyword);

		model.addAttribute("serviceTypeList", serviceTypeSvc.search(normalizedKeyword));

		model.addAttribute("keyword", normalizedKeyword == null ? "" : normalizedKeyword);

		model.addAttribute("loginEmployeeId", loginEmployeeId);

		model.addAttribute("loginEmployeeVO", getLoginEmployeeVO(session));

		return "back-end/service/adminServiceTypeList";
	}

	// =========================================================
	// 新增服務類型
	//
	// POST /admin/service/types/add
	// =========================================================

	@PostMapping("/add")
	public String addServiceType(@RequestParam String typeName, @RequestParam(required = false) String description,
			HttpSession session, RedirectAttributes redirectAttributes) {

		Integer loginEmployeeId = getLoginEmployeeId(session);

		if (loginEmployeeId == null) {
			return "redirect:" + BASE_PATH + "/fake-login";
		}

		try {

			ServiceTypeVO serviceTypeVO = serviceTypeSvc.add(typeName, normalizeNullableText(description));

			redirectAttributes.addFlashAttribute("successMsg", "服務類型「" + serviceTypeVO.getTypeName() + "」新增成功");

		} catch (IllegalArgumentException e) {

			redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
		}

		return "redirect:" + BASE_PATH;
	}

	// =========================================================
	// 修改服務類型
	//
	// POST /admin/service/types/{serviceTypeId}/update
	// =========================================================

	@PostMapping("/{serviceTypeId}/update")
	public String updateServiceType(@PathVariable Integer serviceTypeId, @RequestParam String typeName,
			@RequestParam(required = false) String description, HttpSession session,
			RedirectAttributes redirectAttributes) {

		Integer loginEmployeeId = getLoginEmployeeId(session);

		if (loginEmployeeId == null) {
			return "redirect:" + BASE_PATH + "/fake-login";
		}

		try {

			ServiceTypeVO updatedType = serviceTypeSvc.update(serviceTypeId, typeName,
					normalizeNullableText(description));

			redirectAttributes.addFlashAttribute("successMsg", "服務類型「" + updatedType.getTypeName() + "」修改成功");

		} catch (IllegalArgumentException e) {

			redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
		}

		return "redirect:" + BASE_PATH;
	}

	// =========================================================
	// 測試用員工假登入
	//
	// GET /admin/service/types/fake-login
	// GET /admin/service/types/fake-login?employeeId=1001
	// =========================================================

	@GetMapping("/fake-login")
	public String fakeLogin(@RequestParam(required = false) Integer employeeId, HttpSession session,
			RedirectAttributes redirectAttributes) {

		if (employeeId == null) {
			employeeId = DEFAULT_FAKE_EMPLOYEE_ID;
		}

		if (employeeId <= 0) {

			redirectAttributes.addFlashAttribute("errorMsg", "員工編號不正確");

			return "redirect:" + BASE_PATH;
		}

		EmployeeVO employeeVO = new EmployeeVO();

		employeeVO.setEmployeeId(employeeId);

		session.setAttribute(EMPLOYEE_SESSION_KEY, employeeVO);

		redirectAttributes.addFlashAttribute("successMsg", "已假登入員工 #" + employeeId);

		return "redirect:" + BASE_PATH;
	}

	// =========================================================
	// 測試用員工假登出
	//
	// GET /admin/service/types/fake-logout
	// =========================================================

	@GetMapping("/fake-logout")
	public String fakeLogout(HttpSession session, RedirectAttributes redirectAttributes) {

		session.removeAttribute(EMPLOYEE_SESSION_KEY);

		redirectAttributes.addFlashAttribute("successMsg", "員工 Session 已清除");

		/*
		 * 返回服務類型頁面後， 因為 EmployeeVO 已清除， listServiceTypes() 會再次導向假登入。
		 */
		return "redirect:" + BASE_PATH;
	}

	// =========================================================
	// 共用：取得 Session 中的 EmployeeVO
	// =========================================================

	private EmployeeVO getLoginEmployeeVO(HttpSession session) {

		Object sessionObject = session.getAttribute(EMPLOYEE_SESSION_KEY);

		if (sessionObject instanceof EmployeeVO employeeVO) {
			return employeeVO;
		}

		return null;
	}

	// =========================================================
	// 共用：取得登入員工編號
	// =========================================================

	private Integer getLoginEmployeeId(HttpSession session) {

		EmployeeVO employeeVO = getLoginEmployeeVO(session);

		if (employeeVO == null) {
			return null;
		}

		return employeeVO.getEmployeeId();
	}

	// =========================================================
	// 共用：空白文字轉成 null
	// =========================================================

	private String normalizeNullableText(String text) {

		if (text == null) {
			return null;
		}

		String normalized = text.trim();

		return normalized.isEmpty() ? null : normalized;
	}
}