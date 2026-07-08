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

import com.webond.platform.model.FaqVO;
import com.webond.platform.service.FaqService;

@Controller
@RequestMapping("/faqs")
public class FaqFrontController {

	@Autowired
	private FaqService faqSvc;

	// ===== FAQ列表（只顯示已發布，可用類型 + 問題關鍵字搜尋） =====
	@GetMapping
	public String list(@RequestParam(value = "faqType", required = false) Byte faqType,
			@RequestParam(value = "keyword", required = false) String keyword, ModelMap model) {

		List<FaqVO> list = faqSvc.getByStatus(FaqService.STATUS_PUBLISHED);

		if (faqType != null) {
			list = list.stream().filter(faq -> faqType.equals(faq.getFaqType())).toList();
		}

		if (keyword != null && !keyword.isBlank()) {
			list = list.stream().filter(faq -> (faq.getQuestion() != null && faq.getQuestion().contains(keyword))
					|| (faq.getAnswer() != null && faq.getAnswer().contains(keyword))).toList();
		}

		model.addAttribute("faqListData", list);
		model.addAttribute("searchFaqType", faqType);
		model.addAttribute("searchKeyword", keyword);
		return "front-end/faq/myFaq";
	}

	// ===== FAQ內容（單筆，僅限已發布） =====
	@GetMapping("{faqId}")
	public String detail(@PathVariable Integer faqId, ModelMap model) {
		FaqVO faqVO = faqSvc.getOneFaq(faqId);
		if (faqVO == null || faqVO.getStatus() != FaqService.STATUS_PUBLISHED) {
			// 不存在或還是草稿：導回列表，不透露任何錯誤細節
			return "redirect:/faqs";
		}
		model.addAttribute("faqVO", faqVO);
		return "front-end/faq/myFaqDetail";
	}

	// ===== 提供FAQ類型清單給下拉選單使用 =====
	@ModelAttribute("faqTypeListData")
	protected Map<Byte, String> referenceFaqTypeList() {
		return FaqService.FAQ_TYPE_LABELS;
	}
}
