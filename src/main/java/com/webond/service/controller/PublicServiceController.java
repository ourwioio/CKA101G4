package com.webond.service.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.webond.member.model.MemberVO;
import com.webond.service.model.ServiceSlotVO;
import com.webond.service.model.ServiceTypeVO;
import com.webond.service.model.ServiceVO;
import com.webond.service.service.ServiceOrderService;
import com.webond.service.service.ServiceService;
import com.webond.service.service.ServiceSlotService;
import com.webond.service.service.ServiceTypeService;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/front/services")
public class PublicServiceController {

	private final ServiceService serviceSvc;
	private final ServiceTypeService serviceTypeSvc;
	private final ServiceSlotService serviceSlotSvc;
	private final ServiceOrderService serviceOrderSvc;

	public PublicServiceController(ServiceService serviceSvc, ServiceTypeService serviceTypeSvc,
			ServiceSlotService serviceSlotSvc, ServiceOrderService serviceOrderSvc) {
		this.serviceSvc = serviceSvc;
		this.serviceTypeSvc = serviceTypeSvc;
		this.serviceSlotSvc = serviceSlotSvc;
		this.serviceOrderSvc = serviceOrderSvc;
	}

	// 前台公開服務列表：查詢所有已上架服務與服務類型
	// URL: GET /front/services
	@GetMapping
	public String listService(Model model) {

		List<ServiceVO> serviceList = serviceSvc.getActiveServices();
		List<ServiceTypeVO> serviceTypeList = serviceTypeSvc.getAll();

		model.addAttribute("serviceList", serviceList);
		model.addAttribute("serviceTypeList", serviceTypeList);

		return "front-end/service/serviceList";
	}

	// 前台服務詳情：依服務 ID 查詢已上架服務資料與該服務時段
	// URL: GET /front/services/{serviceId}
	@GetMapping("/{serviceId}")
	public String getServiceDetail(@PathVariable Integer serviceId, Model model) {

		ServiceVO serviceVO = serviceSvc.getActiveServiceById(serviceId);

		if (serviceVO == null) {
			model.addAttribute("errorMsg", "查無此服務或此服務目前未上架");
			model.addAttribute("serviceList", serviceSvc.getActiveServices());
			model.addAttribute("serviceTypeList", serviceTypeSvc.getAll());

			return "front-end/service/serviceList";
		}

		List<ServiceSlotVO> serviceSlotList = serviceSlotSvc.getByServiceId(serviceId);

		model.addAttribute("serviceVO", serviceVO);
		model.addAttribute("serviceSlotList", serviceSlotList);

		return "front-end/service/serviceDetail";
	}

	// 前台依服務類型查詢已上架服務列表
	// URL: GET /front/services/type/{serviceTypeId}
	@GetMapping("/type/{serviceTypeId}")
	public String listServicesByType(@PathVariable Integer serviceTypeId, Model model) {

		List<ServiceVO> serviceList = serviceSvc.getActiveServicesByServiceTypeId(serviceTypeId);
		List<ServiceTypeVO> serviceTypeList = serviceTypeSvc.getAll();

		model.addAttribute("serviceList", serviceList);
		model.addAttribute("serviceTypeList", serviceTypeList);
		model.addAttribute("selectedServiceTypeId", serviceTypeId);

		return "front-end/service/serviceList";
	}

	// 前台依關鍵字搜尋已上架服務
	// URL: GET /front/services/search?keyword=Java
	@GetMapping("/search")
	public String searchServices(@RequestParam(required = false) String keyword, Model model) {

		List<ServiceVO> serviceList = serviceSvc.searchActiveServices(keyword);
		List<ServiceTypeVO> serviceTypeList = serviceTypeSvc.getAll();

		model.addAttribute("serviceList", serviceList);
		model.addAttribute("serviceTypeList", serviceTypeList);
		model.addAttribute("keyword", keyword);

		return "front-end/service/serviceList";
	}

	// 買家送出預約申請
	// URL: POST /front/services/{serviceId}/slots/{serviceSlotId}/request
	@PostMapping("/{serviceId}/slots/{serviceSlotId}/request")
	public String createServiceRequest(@PathVariable Integer serviceId, @PathVariable Integer serviceSlotId,
			@RequestParam(required = false) String buyerRequestNote, HttpSession session,
			RedirectAttributes redirectAttributes) {

		MemberVO memberVO = (MemberVO) session.getAttribute("memberVO");

		if (memberVO == null) {
			redirectAttributes.addFlashAttribute("errorMsg", "請先登入後再預約");
			return "redirect:/member/services/fakelogin";
		}

		Integer buyerMemberId = memberVO.getMemberId();

		try {
			serviceOrderSvc.createRequest(serviceSlotId, buyerMemberId, buyerRequestNote);

			redirectAttributes.addFlashAttribute("successMsg", "預約申請已送出，請等待賣家確認");

		} catch (RuntimeException e) {
			redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
		}

		return "redirect:/front/services/" + serviceId;
	}

//顯示服務圖片
//如果服務沒有上傳圖片，就顯示預設圖
//URL: GET /front/services/image/{serviceId}
	@GetMapping("/image/{serviceId}")
	public void showServiceImage(@PathVariable Integer serviceId, HttpServletResponse response) throws IOException {

		ServiceVO serviceVO = serviceSvc.getOneService(serviceId);

		if (serviceVO != null && serviceVO.getServiceImage() != null && serviceVO.getServiceImage().length > 0) {

			String imageType = serviceVO.getServiceImageType();

			if (imageType == null || imageType.isBlank()) {
				imageType = MediaType.IMAGE_JPEG_VALUE;
			}

			response.setContentType(imageType);
			response.getOutputStream().write(serviceVO.getServiceImage());
			return;
		}

		// 沒有圖片時，改讀 src/main/resources/static/images/activity/default-activity.jpg
		ClassPathResource defaultImage = new ClassPathResource("static/images/activity/default-activity.jpg");

		response.setContentType(MediaType.IMAGE_JPEG_VALUE);
		StreamUtils.copy(defaultImage.getInputStream(), response.getOutputStream());
	}
}