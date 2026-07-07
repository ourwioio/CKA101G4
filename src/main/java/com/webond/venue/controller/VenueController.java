package com.webond.venue.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.webond.venue.model.VenueTypeVO;
import com.webond.venue.model.VenueVO;
import com.webond.venue.service.VenueImagesService;
import com.webond.venue.service.VenueService;
import com.webond.venue.service.VenueTypeService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/venue")
public class VenueController {

	@Autowired
	VenueService venueService;

	@Autowired
	VenueImagesService venueImagesService;

	@Autowired
	VenueTypeService venueTypeService;

	@GetMapping("index")
	public String index() {
		return "back-end/venue/index";
	}
	
	@PostMapping("getOne_For_Update")
	public String getOne_For_Update(@RequestParam("venueId") String venueId, ModelMap model) {
		/*************************** 1.接收請求參數 - 輸入格式的錯誤處理 ************************/
		/*************************** 2.開始查詢資料 *****************************************/
		VenueVO venueVO = venueService.getOneVenue(Integer.valueOf(venueId));

		/*************************** 3.查詢完成,準備轉交(Send the Success view) **************/
		model.addAttribute("venueVO", venueVO);
		return "back-end/venue/update_venue_input";
	}

	@PostMapping("update")
	public String update(@Valid VenueVO venueVO, BindingResult result, ModelMap model,
			@RequestParam("upFiles") MultipartFile[] parts,
			@RequestParam(value = "openDays", required = false) List<Integer> openDays,
			@RequestParam("startHour") int startHour, @RequestParam("endHour") int endHour) throws IOException {

		if (result.hasErrors()) {
			return "back-end/venue/update_venue_input";
		}

		// 處理開放日與營業時間
		StringBuilder daysSb = new StringBuilder("0000000");
		if (openDays != null) {
			for (Integer day : openDays) {
				if (day >= 0 && day <= 6)
					daysSb.setCharAt(day, '1');
			}
		}
		venueVO.setOpenDays(daysSb.toString());

		StringBuilder hoursSb = new StringBuilder("222222222222222222222222");
		for (int h = startHour; h < endHour; h++) {
			if (h >= 0 && h < 24)
				hoursSb.setCharAt(h, '0');
		}
		venueVO.setAvailableHours(hoursSb.toString());

		// 準備新照片
		List<byte[]> imageBytesList = new ArrayList<>();
		if (parts != null && parts.length > 0 && !parts[0].isEmpty()) {
			for (MultipartFile multipartFile : parts) {
				if (!multipartFile.isEmpty()) {
					imageBytesList.add(multipartFile.getBytes());
				}
			}
		}

		// 執行更新
		venueService.updateVenueWithImages(venueVO, imageBytesList);

		// 修改完成，撈最新資料
		model.addAttribute("success", "- (修改成功)");
		venueVO = venueService.getOneVenue(venueVO.getVenueId());
		model.addAttribute("venueVO", venueVO);

		return "back-end/venue/listOneVenue";
	}

	@PostMapping("delete")
	public String delete(@RequestParam("venueId") String venueId, ModelMap model) {
		venueService.deleteVenue(Integer.valueOf(venueId));

		List<VenueVO> list = venueService.getAll();
		model.addAttribute("venueListData", list);
		model.addAttribute("success", "刪除成功");
		return "back-end/venue/listAllVenue";
	}

	
	
	@GetMapping("getOneVenue")
	public String getOne(@RequestParam("venueId") Integer venueId, ModelMap model) {
		VenueVO venueVO = venueService.getOneVenue(venueId);
		model.addAttribute("venueVO", venueVO);
		return "back-end/venue/listOneVenue";
	}

	@GetMapping("deleteImage")
	public String deleteImage(@RequestParam("imagesId") Integer imagesId, @RequestParam("venueId") Integer venueId) {
		venueImagesService.deleteImage(imagesId);
		return "redirect:/venue/getOne_For_Update?venueId=" + venueId;
	}

	@PostMapping("toggleStatus")
	public String toggleStatus(@RequestParam("venueId") Integer venueId) {
		venueService.toggleVenueStatus(venueId);
		return "redirect:/venue/listAllVenue";
	}

	@ModelAttribute("venueTypeData")
	protected List<VenueTypeVO> venueTypeListData() {
		List<VenueTypeVO> list = venueTypeService.getAll();
		return list;
	}

	@GetMapping("listAllVenue")
	public String listAllVenue(ModelMap model) {
		List<VenueVO> list = venueService.getAll();
		model.addAttribute("venueListData", list);
		return "back-end/venue/listAllVenue";
	}
	
	
	// 去除BindingResult中某個欄位的FieldError紀錄
	public BindingResult removeFieldError(VenueVO venueVO, BindingResult result, String removedFieldname) {
		// 從原BindingResult中去除removedFieldname這個欄位的紀錄之後，再將其它所保留下來的欄位的FieldError紀錄轉換成errorsListToKeep這個List物件
		List<FieldError> errorsListToKeep = result.getFieldErrors().stream()
				.filter(fieldError -> !fieldError.getField().equals(removedFieldname)).collect(Collectors.toList());
		// 對驗證的目標對象建立一個新(空)的BindingResult的物件
		// 參數一：目標對象
		// 參數二：對象的名稱(通常是類別名首字母小寫)
		result = new BeanPropertyBindingResult(venueVO, "venueVO");
		// 將新(空)的BindingResult的物件加入保留下來的其它欄位的FieldError紀錄
		for (FieldError fieldError : errorsListToKeep) {
			result.addError(fieldError);
		}
		// 更新後的BindingResult
		return result;
	}

}