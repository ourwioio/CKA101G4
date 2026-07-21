package com.webond.employee.controller;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URLConnection;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.webond.employee.model.EmpService;
import com.webond.employee.model.EmployeeVO;
import com.webond.employee.model.PermissionService;
import com.webond.employee.model.PermissionVO;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/admin/employees")
public class EmpController {

	@Autowired
	EmpService empSvc;

	@Autowired
	PermissionService permSvc;

// === 所有員工 === //
	@GetMapping("/empPage")
	public String getEmpList(@RequestParam(value = "searchEmpName", required = false) String searchEmpName,
			@RequestParam(value = "searchRoleTitle", required = false) Integer searchRoleTitle,
			@RequestParam(value = "searchEmpStatus", required = false) Integer searchEmpStatus,
			@RequestParam(value = "p", defaultValue = "0") Integer p, Model model,
			@RequestParam(value = "openAddEmpModal", required = false) Boolean openAddEmpModal,
			@RequestParam(value = "openEditEmpModal", required = false) Boolean openEditEmpModal) {

		// 呼叫Service拿資料
		Page<EmployeeVO> empPageData = empSvc.getAllByPage(searchEmpName, searchRoleTitle, searchEmpStatus, p);

		// 對應前端
		model.addAttribute("empListData", empPageData.getContent());
		model.addAttribute("currentPage", p); // 當前頁碼（從 0 開始）
		model.addAttribute("totalPages", empPageData.getTotalPages()); // 總頁數

		model.addAttribute("currentEmpName", searchEmpName);
		model.addAttribute("currentRoleTitle", searchRoleTitle);
		model.addAttribute("currentEmpStatus", searchEmpStatus);

		// =========新遭按鈕用========================//
		model.addAttribute("openAddEmpModal", openAddEmpModal);
		// =========修改按鈕用========================//
		model.addAttribute("openEditEmpModal", openEditEmpModal);
		// =========燈箱用(權限)===================//
		List<PermissionVO> permListData = permSvc.getAll();
		model.addAttribute("permVO", permListData);
		if (!model.containsAttribute("employeeVO")) {
			model.addAttribute("employeeVO", new EmployeeVO());
		}

		if (!model.containsAttribute("addEmployeeVO")) {
			model.addAttribute("addEmployeeVO", new EmployeeVO());
		}

		// 指定頁面
		return "back-end/employee/empPage";

	}

//	=========================查詢所有員工的圖片
	@GetMapping("/empImg/{employeeId}")
	@ResponseBody
	public ResponseEntity<byte[]> getEmpImage(@PathVariable Integer employeeId) {

		EmployeeVO empVO = empSvc.getOneEmp(employeeId);
		if (empVO == null) {
			System.out.println("查無此員工");
		}

		byte[] imgBytes = empVO.getEmpImg();
		String imgType = "image/jpeg";

		try {
			imgType = URLConnection
					.guessContentTypeFromStream(new BufferedInputStream(new ByteArrayInputStream(imgBytes)));

			if (imgType == null) {
				imgType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
			}
		} catch (Exception e) {
			System.out.println("圖片格式錯誤");
		}

		return ResponseEntity.ok().contentType(MediaType.parseMediaType(imgType))
				.header("Cache-Control", "max-age=3600").body(imgBytes);

	}

//====新增員工燈箱畫面=========================//
	@GetMapping("/empPage/addEmpModal")
	public String getAddEmpModal(Model model) {

		// 1. 補上空的表單物件，供 Thymeleaf 的 th:object 表單綁定使用
		if (!model.containsAttribute("employeeVO")) {
			model.addAttribute("employeeVO", new EmployeeVO());
		}

		// ====新增用到的permissionVO選項====//
		List<PermissionVO> permVO = permSvc.getAll();
		model.addAttribute("permVO", permVO);

		return "back-end/employee/addEmp :: addContent";

	}

//=====新增員工  (抓確認送出資料)========================//
	@PostMapping("/add")
	public String insert(@Valid EmployeeVO employeeVO, BindingResult result, ModelMap model,
			@RequestParam(value = "permIds", required = false) List<Integer> permIds, // 手動接收前端Checkbox陣列
			@RequestParam("upImg") MultipartFile upImg,
			@RequestParam(value = "currentPage", defaultValue = "0") int currentPage,
			@RequestParam(value = "searchEmpName", required = false) String searchEmpName, 
			@RequestParam(value = "searchRoleTitle", required = false) Integer searchRoleTitle, 
			@RequestParam(value = "searchEmpStatus", required = false) Integer searchEmpStatus,
			HttpSession session,
			RedirectAttributes redirectAttributes) throws IOException {

		/*************************** 1.接收請求參數 - 輸入格式的錯誤處理 ************************/
		// 排除前端傳來List<Integer>導致的empPermVO (<EmpPermVO>)屬性型態不合產生的錯誤
		result = removeFieldError(employeeVO, result, "empPermVO");

		// 權限範圍不能空白
		if (permIds == null || permIds.isEmpty()) {
			result.rejectValue("empPermVO", "permIdsBlank", "權限範圍請勿空白");
		}

		// 檢查帳號唯一性
		// 避免a123,A123(轉小寫)
		if (!result.hasErrors() && employeeVO.getEmpAccount() != null) {
			String targetUsername = employeeVO.getEmpAccount().trim().toLowerCase();
			employeeVO.setEmpAccount(targetUsername);

			if (empSvc.isEmpAccountExists(targetUsername)) {
				result.rejectValue("empAccount", "empAccountDuplicate", "已存在此帳號");
			}
		}

		// 密碼格式
		if (!result.hasErrors() && employeeVO.getEmpPassword() != null) {
			String regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z\\d]{8,20}$";
			String upPassword = employeeVO.getEmpPassword();

			if (!upPassword.matches(regexp)) {
				result.rejectValue("empPassword", "empPasswordPattern", "密碼長度為 8 到 20 的大小寫英文與數字");
			}

		}

		// 照片的錯誤處理
		if (!result.hasErrors() && upImg != null && !upImg.isEmpty()) {
			// 限制檔案大小
			if (upImg.getSize() > 5 * 1024 * 1024) {
				result.rejectValue("empImg", "empImgSize", "上傳的圖片檔案不能超過5MB");
			}
			// 限制必須是圖片格式
			String contentType = upImg.getContentType();
			if (contentType == null || !contentType.startsWith("image/")) {
				result.rejectValue("empImg", "empImgType", "只能上傳圖片檔案(jpg, png 等)");
			}
		}

		// 檢查其他錯誤格式
		if (result.hasErrors()) {

			if (upImg != null && !upImg.isEmpty()) {
				session.setAttribute("addSessionImg", upImg.getBytes());
				employeeVO.setEmpImg(null);
			} else {
				Object imgObj = session.getAttribute("addSessionImg");
				if (imgObj instanceof byte[]) {
					employeeVO.setEmpImg(null);
				}
			}

			if (session.getAttribute("addSessionImg") != null) {
				model.addAttribute("hasTempImg", true);
			}



			StringBuilder errorUrl = new StringBuilder("redirect:/admin/employees/empPage?p=" + currentPage);
			errorUrl.append("&openAddEmpModal=true");

			if (searchEmpName != null && !searchEmpName.trim().isEmpty()) {
	            errorUrl.append("&searchEmpName=").append(searchEmpName.trim());
	        }
	        if (searchRoleTitle != null) {
	            errorUrl.append("&searchRoleTitle=").append(searchRoleTitle);
	        }
	        if (searchEmpStatus != null) {
	            errorUrl.append("&searchEmpStatus=").append(searchEmpStatus);
	        }
	        
			redirectAttributes.addFlashAttribute(BindingResult.MODEL_KEY_PREFIX + "addEmployeeVO", result);
			redirectAttributes.addFlashAttribute("addEmployeeVO", employeeVO);
			redirectAttributes.addFlashAttribute("selectedPermIds", permIds);
			
			return errorUrl.toString();
		}

		/*************************** 2.開始新增資料 *****************************************/

		byte[] finalImgBytes = null;

		if (upImg != null && !upImg.isEmpty()) {
			finalImgBytes = upImg.getBytes();
		} else {
			// 【安全防禦修改】：安全地從 Session 取出圖片
			Object imgObj = session.getAttribute("addSessionImg");
			if (imgObj instanceof byte[]) { // 確保型別安全，防止轉型錯誤
				finalImgBytes = (byte[]) imgObj;
			}
		}

		employeeVO.setEmpImg(finalImgBytes);

		empSvc.saveEmp(employeeVO, permIds, upImg);

		session.removeAttribute("addSessionImg");

		/*************************** 3.新增完成,準備轉交(Send the Success view) **************/
		List<EmployeeVO> list = empSvc.getAll();
		model.addAttribute("empListData", list);
		StringBuilder redirectUrl = new StringBuilder("redirect:/admin/employees/empPage?p=" + currentPage);
		if (searchEmpName != null && !searchEmpName.trim().isEmpty()) {
		    redirectUrl.append("&searchEmpName=").append(searchEmpName.trim());
		}
		if (searchRoleTitle != null) {
		    redirectUrl.append("&searchRoleTitle=").append(searchRoleTitle);
		}
		if (searchEmpStatus != null) {
		    redirectUrl.append("&searchEmpStatus=").append(searchEmpStatus);
		}

		return redirectUrl.toString();
	}

//=== 新增的圖片處理 === //
	@GetMapping("/dbgAddImg")
	public void dbgAddImg(HttpServletResponse response, HttpSession session) throws IOException {

		Object imgObj = session.getAttribute("addSessionImg");

		if (imgObj instanceof byte[]) {
			byte[] imageBytes = (byte[]) imgObj;

			if (imageBytes != null && imageBytes.length > 0) {
				response.setContentType("image/jpeg");
				try (ServletOutputStream out = response.getOutputStream()) {
					out.write(imageBytes);
					out.flush();
				}
				return;
			}
		}

	}

//====修改員工燈箱畫面=========================//
	@GetMapping("/empPage/editEmpModal")
	public String getEditEmpModal(Model model) {

		if (!model.containsAttribute("employeeVO")) {
			model.addAttribute("employeeVO", new EmployeeVO());
		}

		List<PermissionVO> permVO = permSvc.getAll();
		model.addAttribute("permVO", permVO);

		return "back-end/employee/updateEmp :: updateContent";

	}

//=====修改員工  (抓確認送出資料)========================//
	@PostMapping("/update")
	public String update(@Valid EmployeeVO employeeVO, BindingResult result, ModelMap model,
			@RequestParam(value = "permIds", required = false) List<Integer> permIds, // 手動接收前端Checkbox陣列
			@RequestParam("upImg") MultipartFile upImg,
			@RequestParam(value = "currentPage", defaultValue = "0") int currentPage,
			@RequestParam(value = "searchEmpName", required = false) String searchEmpName, 
	        @RequestParam(value = "searchRoleTitle", required = false) Integer searchRoleTitle, 
	        @RequestParam(value = "searchEmpStatus", required = false) Integer searchEmpStatus,
			HttpSession session,
			RedirectAttributes redirectAttributes)
			throws IOException {

		/*************************** 1.接收請求參數 - 輸入格式的錯誤處理 ************************/
		result = removeFieldError(employeeVO, result, "empPermVO");

		// 把出錯時上傳的圖片填進去
		byte[] sessionImg = (byte[]) session.getAttribute("sessionImg" + employeeVO.getEmployeeId());
		employeeVO = empSvc.processEmpImg(employeeVO, upImg, sessionImg);

		// 權限範圍不能空白
		if (permIds == null || permIds.isEmpty()) {
			result.rejectValue("empPermVO", "permIdsBlank", "權限範圍請勿空白");
			result.reject( "permIdsBlank", "權限範圍請勿空白");
		}

		// 檢查其他錯誤格式
		if (result.hasErrors()) {
			if (upImg != null && !upImg.isEmpty()) {
				session.setAttribute("sessionImg" + employeeVO.getEmployeeId(), upImg.getBytes());
			}

			redirectAttributes.addFlashAttribute(BindingResult.MODEL_KEY_PREFIX + "employeeVO", result);
	        redirectAttributes.addFlashAttribute("employeeVO", employeeVO);
	        redirectAttributes.addFlashAttribute("selectedPermIds", permIds);
	        
	        redirectAttributes.addFlashAttribute("openEditEmpModal", true);

	        StringBuilder errorUrl = new StringBuilder("redirect:/admin/employees/empPage?p=" + currentPage);
	        errorUrl.append("&openEditEmpModal=true");
	        
	        if (searchEmpName != null && !searchEmpName.trim().isEmpty()) {
	            errorUrl.append("&searchEmpName=").append(searchEmpName.trim());
	        }
	        if (searchRoleTitle != null) {
	            errorUrl.append("&searchRoleTitle=").append(searchRoleTitle);
	        }
	        if (searchEmpStatus != null) {
	            errorUrl.append("&searchEmpStatus=").append(searchEmpStatus);
	        }
	        
	        return errorUrl.toString();
		}

		/*************************** 2.開始修改資料 *****************************************/
		empSvc.updateEmp(employeeVO, permIds, upImg);
		session.removeAttribute("sessionImg" + employeeVO.getEmployeeId());
		/*************************** 3.修改完成,準備轉交(Send the Success view) **************/
		StringBuilder redirectUrl = new StringBuilder("redirect:/admin/employees/empPage?p=" + currentPage);
		if (searchEmpName != null && !searchEmpName.trim().isEmpty()) {
	        redirectUrl.append("&searchEmpName=").append(searchEmpName.trim());
	    }
	    if (searchRoleTitle != null) {
	        redirectUrl.append("&searchRoleTitle=").append(searchRoleTitle);
	    }
	    if (searchEmpStatus != null) {
	        redirectUrl.append("&searchEmpStatus=").append(searchEmpStatus);
	    }

		return redirectUrl.toString();
	}

//=== 修改的圖片處理 === //
	@GetMapping("/dbgUpdateImg/{employeeId}")
	public void dbgImg(@PathVariable("employeeId") Integer employeeId, HttpServletResponse response,
			HttpSession session, Model model) throws IOException {

		byte[] imageBytes = (byte[]) session.getAttribute("sessionImg" + employeeId);

		if (imageBytes == null) {
			imageBytes = empSvc.getEmpImage(employeeId);
		}

		if (imageBytes != null && imageBytes.length > 0) {
			response.setContentType("image/jpeg");
			ServletOutputStream out = response.getOutputStream();
			out.write(imageBytes);
			out.flush();
			out.close();
		}

	}

	// === 刪除員工(確認送出) ===//
	@PostMapping("/empDelete")
	public String deleteEmp(
			@RequestParam("employeeId") Integer employeeId, 
			@RequestParam(value = "currentPage", defaultValue = "0") int currentPage,
	        @RequestParam(value = "searchEmpName", required = false) String searchEmpName, 
	        @RequestParam(value = "searchRoleTitle", required = false) Integer searchRoleTitle, 
	        @RequestParam(value = "searchEmpStatus", required = false) Integer searchEmpStatus,
	        RedirectAttributes redirectAttributes) {
		/*************************** 1.接收請求參數 - 輸入格式的錯誤處理 ************************/
		/*************************** 2.開始刪除資料 *****************************************/
		empSvc.deleteEmp(Integer.valueOf(employeeId));
		/*************************** 3.刪除完成,準備轉交(Send the Success view) **************/
		redirectAttributes.addFlashAttribute("successMessage", "🗑️ 員工資料已成功刪除！");

	    StringBuilder redirectUrl = new StringBuilder("redirect:/admin/employees/empPage?p=" + currentPage);
	    if (searchEmpName != null && !searchEmpName.trim().isEmpty()) {
	        redirectUrl.append("&searchEmpName=").append(searchEmpName.trim());
	    }
	    if (searchRoleTitle != null) {
	        redirectUrl.append("&searchRoleTitle=").append(searchRoleTitle);
	    }
	    if (searchEmpStatus != null) {
	        redirectUrl.append("&searchEmpStatus=").append(searchEmpStatus);
	    }

	    return redirectUrl.toString();

	}

// === 新的紀錄本 ===//
	public BindingResult removeFieldError(EmployeeVO employeeVO, BindingResult result, String removedFieldname) {
		List<FieldError> errorsListTokeep = result.getFieldErrors().stream()
				.filter(fieldError -> !fieldError.getField().equals(removedFieldname)).collect(Collectors.toList());
		result = new BeanPropertyBindingResult(employeeVO, "employeeVO");

		for (FieldError fieldError : errorsListTokeep) {
			result.addError(fieldError);
		}
		return result;

	}


}
