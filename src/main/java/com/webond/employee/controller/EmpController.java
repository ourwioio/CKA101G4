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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.webond.employee.model.EmpService;
import com.webond.employee.model.EmployeeVO;
import com.webond.employee.model.PermissionService;
import com.webond.employee.model.PermissionVO;

import jakarta.validation.Valid;

@Controller
public class EmpController {

	@Autowired
	EmpService empSvc;
	
	@Autowired
	PermissionService permSvc;

//	 =====================查詢所有員工
	@GetMapping("/admin/empPage")
	public String getEmpList(
			// 接收前端網址列傳來的 page 參數（例如：/emp/empAll?page=1）
	        // 如果前端第一次進來沒傳參數，預設給 0（代表第 1 頁）
			@RequestParam(value = "p", defaultValue = "0") Integer p, Model model,
			// 補上這行：手動接收網址傳來的 openAddModal 參數（如果是點擊超連結過來的，就會是 true）
	        @RequestParam(value = "openAddModal", required = false) Boolean openAddModal) {
		
		
//====== 全部員工========//
		// 呼叫Service拿資料
		Page<EmployeeVO> empPage = empSvc.getAllByPage(p);

		// 對應前端
		model.addAttribute("empListData", empPage.getContent());
		
	    // 傳遞分頁控制參數給前端 Thymeleaf 畫版
	    model.addAttribute("currentPage", p);                    // 當前頁碼（從 0 開始）
	    model.addAttribute("totalPages", empPage.getTotalPages());   // 總頁數
	    
	    
	    
	    
		
	  //=========新遭按鈕用========================//
	    model.addAttribute("openAddModal", openAddModal);
	  //=========新增燈箱用(權限)===================//
        List<PermissionVO> permListData = permSvc.getAll();
        model.addAttribute("permVO", permListData);
		
      //==========新增燈箱用(給空empVO)================//
        model.addAttribute("employeeVO", new EmployeeVO());
        
      
		

		
		// 指定頁面
		return "back-end/employee/empPage";
		
	}

	
	
//	=========================查詢所有員工的圖片
	// @GetMapping("/employee/photo/{id}")：定義一個動態路由。{id} 是一個預留位置（Placeholder），代表員工的
	// ID。
	// @PathVariable Long id：把網址列上的 {id} 傳進來當作方法的參數（例如網址是 /employee/photo/5，那 id 就等於
	// 5）。
	// @ResponseBody：非常重要！ 預設情況下，@Controller 裡的方法回傳 String 時，Spring 會去尋找對應的 HTML
	// 檔案（模板）。加上 @ResponseBody 就是告訴 Spring：
	// ResponseEntity<byte[]>：這是 Spring 提供的強大工具，用來精準控制 HTTP 回傳狀態（如 200 OK、404 Not
	// Found）、Header（回傳標頭）以及 Body（這裡就是圖片的二進位陣列 byte[]）。
	@GetMapping("/admin/empImg/{employeeId}")
	@ResponseBody
	public ResponseEntity<byte[]> getEmpImage(@PathVariable Integer employeeId) {

		// 如果透過ID查不到員工
		EmployeeVO empVO = empSvc.getOneEmp(employeeId);
		if (empVO == null) {
			System.out.println("查無此員工");
		}

		
		byte[] imgBytes = empVO.getEmpImg();
		// 預設格式，防範無法偵測時的狀況
		String imgType = "image/jpeg";

		try {
			// 動態從二進位資料串流中，偵測圖片的正確 Type (例如: image/png)
			imgType = URLConnection
					.guessContentTypeFromStream(new BufferedInputStream(new ByteArrayInputStream(imgBytes)));

			// 如果完全偵測不出來，給予通用的二進位串流格式
			if (imgType == null) {
				imgType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
			}
		} catch (Exception e) {
			System.out.println("圖片格式錯誤");
		}

		// ResponseEntity.ok()：設定 HTTP 狀態碼為 200 OK，代表請求成功。
		return ResponseEntity.ok()
				// 動態帶入偵測到的 img Type 字串
				.contentType(MediaType.parseMediaType(imgType))
				// 效能優化，告訴瀏覽器把這張圖片快取（Cache）3600 秒（1 小時）。
				.header("Cache-Control", "max-age=3600")
				// .body(photoBytes)
				.body(imgBytes);

	}
	
//====新增員工燈箱畫面=========================//
	@GetMapping("/empPage/addEmpModel")
	public String getAddEmpModal(Model model) {
		
		// ====新增用到的permissionVO選項====//
		List<PermissionVO> permVO = permSvc.getAll();
		model.addAttribute("permVO", permVO);
		
		return "back-end/employee/addEmp :: formContent";
		
		
		
	}
	
	
//=====新增員工  (抓確認送出資料)========================//
	@PostMapping("/admin/insert")
	public String insert(@Valid EmployeeVO empVO, BindingResult result, ModelMap model,
			@RequestParam(value = "permIds", required = false) List<Integer> permIds, //手動接收前端Checkbox陣列
			@RequestParam("upImg") MultipartFile upImg
			) throws IOException {
		
		/*************************** 1.接收請求參數 - 輸入格式的錯誤處理 ************************/
		// 排除前端傳來List<Integer>導致的empPermVO (<EmpPermVO>)屬性型態不合產生的錯誤 
		result = removeFieldError(empVO, result, "empPermVO");
		
		// <姓名>錯誤處理
		
		
		
		// 照片的錯誤處理
		if(upImg !=null && !upImg.isEmpty()) {
			//限制檔案大小
			if(upImg.getSize() > 5 * 1024 * 1024) {
				result.rejectValue("empImg", "empImgSize", "上傳的圖片檔案不能超過5MB");
			}
			// 限制必須是圖片格式
			String contentType = upImg.getContentType();
			if(contentType == null || !contentType.startsWith("image/")) {
				result.rejectValue("empImg", "empImgType", "只能上傳圖片檔案(jpg, png 等)");
			}
		}
		
		
		
	// 檢查帳號唯一性
		//避免a123,A123(轉小寫)
		if (empVO.getEmpAccount() != null) {
			String targetUsername = empVO.getEmpAccount().trim().toLowerCase();
			empVO.setEmpAccount(targetUsername);
			
			if(empSvc.isEmpAccountExists(targetUsername)) {
				 result.rejectValue("empAccount", "empAccountDuplicate", "已存在此帳號");
			}
		}
		
		
	// 檢查其他錯誤格式
		if(result.hasErrors()) {
			// 為了讓燈箱退回原頁面時，畫面的「權限清單 Checkbox」不會消失，必須補塞回 Model
			// 因為送出資料當下的東西相當於送出去後重新渲染，所以需要在抓一次
			List<PermissionVO> permListData = permSvc.getAll();
			model.addAttribute("permVO", permListData);
			
			
			
			
	        // 補齊分頁預設變數，防止燈箱退回時背景的主畫面列表破版
	        Page<EmployeeVO> empPage = empSvc.getAllByPage(0); 
	        model.addAttribute("empListData", empPage.getContent());
	        model.addAttribute("currentPage", 0);
	        model.addAttribute("totalPages", empPage.getTotalPages());
	        // 傳變數給前端，跟他說出錯了，請自動打開(不然出錯可能會關掉?)
	        model.addAttribute("openAddModal", true);
			
	        for (FieldError error : result.getFieldErrors()) {
	            System.out.println("出錯欄位: [" + error.getField() + "] ── 錯誤原因: [" + error.getDefaultMessage() + "]");
	        }
	        System.out.println("=========================================");
			
	        
	     // 塞入一個標記，告訴前端「要把燈箱打開」
	        model.addAttribute("openAddModal", true);
	        return "back-end/employee/empPage";
		}
		
	
		
		
		
		
		
		/*************************** 2.開始新增資料 *****************************************/
			empSvc.saveEmp(empVO, permIds, upImg);
		
		/*************************** 3.新增完成,準備轉交(Send the Success view) **************/
			List<EmployeeVO> list = empSvc.getAll();
			model.addAttribute("empListData", list);
			model.addAttribute("success", "- (新增成功)");
		// 使用 redirect 重新導向回主列表（防止使用者按 F5 重新整理網頁導致重複儲存）
		return "redirect:/admin/empPage";
	}
	
	

	

	
//===== 去除BindingResult中被多對多關聯嵐未誤判的錯誤紀錄===========//
// ===== 建立一個全新的空白記分板，把除了特定被誤判的欄位(removedFieldname)以外的其他真正錯誤都複製過去，最後回傳新的紀錄本
//	BindingResult 物件基於安全設計，「一經建立就只能往上加錯誤，不允許工程師直接手動刪除錯誤」
	public BindingResult removeFieldError(EmployeeVO empVO, BindingResult result, String removedFieldname) {
		List<FieldError> errorsListTokeep = result.getFieldErrors().stream() // 使用 Java Stream API，翻閱原本的紀錄本 
				.filter(fieldError -> !fieldError.getField().equals(removedFieldname)) //只要錯誤欄位不等於特定那個
				.collect(Collectors.toList());  //都收集起來
		// 建立新的紀錄本
		result = new BeanPropertyBindingResult(empVO, "empVO");
		
		// 把真正的錯誤，重新謄寫上去
		for (FieldError fieldError : errorsListTokeep) {
			result.addError(fieldError);
		}
		return result;
		
	}

	
}
