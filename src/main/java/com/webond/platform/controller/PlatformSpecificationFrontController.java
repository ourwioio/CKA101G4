package com.webond.platform.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.webond.platform.model.PlatformSpecificationVO;
import com.webond.platform.service.PlatformSpecificationService;

@Controller
@RequestMapping("/specifications")
public class PlatformSpecificationFrontController {

		@Autowired
		private PlatformSpecificationService specSvc;

		// ===== 規範列表（只顯示已發布，可用類型 + 標題關鍵字搜尋） =====
		@GetMapping
		public String list(
				@RequestParam(value = "specType", required = false) Byte specType,
				@RequestParam(value = "keyword", required = false) String keyword,
				ModelMap model) {

			List<PlatformSpecificationVO> list = specSvc.getByStatus(PlatformSpecificationService.STATUS_PUBLISHED);

			if (specType != null) {
				list = list.stream()
						.filter(spec -> specType.equals(spec.getSpecType()))
						.toList();
			}

			if (keyword != null && !keyword.isBlank()) {
			    list = list.stream()
			            .filter(spec ->
			                    (spec.getTitle() != null && spec.getTitle().contains(keyword)) ||
			                    (spec.getDescription() != null && spec.getDescription().contains(keyword))
			            )
			            .toList();
			}

			model.addAttribute("specListData", list);
			model.addAttribute("searchSpecType", specType);
			model.addAttribute("searchKeyword", keyword);
			return "front-end/platformSpecification/mySpecification";
		}

		// ===== 規範內容（單筆，僅限已發布） =====
		@GetMapping("{specId}")
		public String detail(@PathVariable Integer specId, ModelMap model) {
			PlatformSpecificationVO specVO = specSvc.getOneSpec(specId);
			if (specVO == null || specVO.getStatus() != PlatformSpecificationService.STATUS_PUBLISHED) {
				// 不存在或還是草稿：導回列表，不透露任何錯誤細節
				return "redirect:/specifications";
			}
			model.addAttribute("specVO", specVO);
			return "front-end/platformSpecification/mySpecificationDetail";
		}

		// ===== 提供規範類型清單給下拉選單使用 =====
		@ModelAttribute("specTypeListData")
		protected Map<Byte, String> referenceSpecTypeList() {
			return PlatformSpecificationService.SPEC_TYPE_LABELS;
		}
}
