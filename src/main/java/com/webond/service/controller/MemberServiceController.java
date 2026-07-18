package com.webond.service.controller;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.webond.member.model.MemberVO;
import com.webond.service.dto.ServiceRequest;
import com.webond.service.model.ServiceSlotVO;
import com.webond.service.model.ServiceVO;
import com.webond.service.service.ServiceService;
import com.webond.service.service.ServiceSlotService;
import com.webond.service.service.ServiceTypeService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/member/services")
public class MemberServiceController {

	private static final String LOGIN_URL = "redirect:/member/login";
	private static final String SERVICE_LIST_URL = "redirect:/member/services";

	private final ServiceService serviceSvc;
	private final ServiceTypeService serviceTypeSvc;
	private final ServiceSlotService serviceSlotSvc;

	public MemberServiceController(ServiceService serviceSvc, ServiceTypeService serviceTypeSvc,
			ServiceSlotService serviceSlotSvc) {

		this.serviceSvc = serviceSvc;
		this.serviceTypeSvc = serviceTypeSvc;
		this.serviceSlotSvc = serviceSlotSvc;
	}

	@GetMapping
	public String myServiceList(HttpSession session, Model model) {

		Integer loginMemberId = getLoginMemberId(session);

		if (loginMemberId == null) {
			return LOGIN_URL;
		}

		List<ServiceVO> serviceList = serviceSvc.getManageableServicesByMemberId(loginMemberId);

		model.addAttribute("serviceList", serviceList);
		model.addAttribute("loginMemberId", loginMemberId);

		return "front-end/service/memberServiceList";
	}

	@GetMapping("/add")
	public String showAddForm(HttpSession session, Model model) {

		if (getLoginMemberId(session) == null) {
			return LOGIN_URL;
		}

		prepareServiceForm(model, new ServiceRequest(), "add", null);

		return "front-end/service/memberServiceForm";
	}

	@PostMapping("/add")
	public String addService(@ModelAttribute ServiceRequest request,
			@RequestParam(value = "serviceImageFile", required = false) MultipartFile serviceImageFile,
			HttpSession session, Model model, RedirectAttributes redirectAttributes) {

		Integer loginMemberId = getLoginMemberId(session);

		if (loginMemberId == null) {
			return LOGIN_URL;
		}

		try {
			byte[] serviceImage = getImageBytes(serviceImageFile);

			String serviceImageType = getImageContentType(serviceImageFile);

			serviceSvc.addBySeller(loginMemberId, request.getServiceTypeId(), request.getServiceName(),
					request.getDescription(), request.getHourlyRate(), serviceImage, serviceImageType,
					request.getServiceCity(), request.getServiceDistrict(), request.getServiceLocation());

			redirectAttributes.addFlashAttribute("successMsg", "新增服務成功");

			return SERVICE_LIST_URL;

		} catch (IllegalArgumentException e) {

			prepareServiceForm(model, request, "add", null);

			model.addAttribute("errorMsg", e.getMessage());

			return "front-end/service/memberServiceForm";

		} catch (IOException e) {

			prepareServiceForm(model, request, "add", null);

			model.addAttribute("errorMsg", "圖片讀取失敗，請重新選擇圖片");

			return "front-end/service/memberServiceForm";
		}
	}

	@GetMapping("/{serviceId}/edit")
	public String showEditForm(@PathVariable Integer serviceId, HttpSession session, Model model,
			RedirectAttributes redirectAttributes) {

		Integer loginMemberId = getLoginMemberId(session);

		if (loginMemberId == null) {
			return LOGIN_URL;
		}

		try {
			ServiceVO serviceVO = serviceSvc.getOwnServiceForEdit(serviceId, loginMemberId);

			ServiceRequest request = new ServiceRequest();

			request.setServiceTypeId(serviceVO.getServiceTypeId());

			request.setServiceName(serviceVO.getServiceName());

			request.setDescription(serviceVO.getDescription());

			request.setHourlyRate(serviceVO.getHourlyRate());

			request.setServiceCity(serviceVO.getServiceCity());

			request.setServiceDistrict(serviceVO.getServiceDistrict());

			request.setServiceLocation(serviceVO.getServiceLocation());

			prepareServiceForm(model, request, "edit", serviceId);

			model.addAttribute("serviceVO", serviceVO);

			return "front-end/service/memberServiceForm";

		} catch (IllegalArgumentException e) {

			redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());

			return SERVICE_LIST_URL;
		}
	}

	@PostMapping("/{serviceId}/edit")
	public String updateService(@PathVariable Integer serviceId, @ModelAttribute ServiceRequest request,
			@RequestParam(value = "serviceImageFile", required = false) MultipartFile serviceImageFile,
			HttpSession session, Model model, RedirectAttributes redirectAttributes) {

		Integer loginMemberId = getLoginMemberId(session);

		if (loginMemberId == null) {
			return LOGIN_URL;
		}

		try {
			boolean replaceImage = serviceImageFile != null && !serviceImageFile.isEmpty();

			byte[] serviceImage = getImageBytes(serviceImageFile);

			String serviceImageType = getImageContentType(serviceImageFile);

			serviceSvc.updateBySeller(serviceId, loginMemberId, request.getServiceTypeId(), request.getServiceName(),
					request.getDescription(), request.getHourlyRate(), serviceImage, serviceImageType, replaceImage,
					request.getServiceCity(), request.getServiceDistrict(), request.getServiceLocation());

			redirectAttributes.addFlashAttribute("successMsg", "修改服務成功");

			return SERVICE_LIST_URL;

		} catch (IllegalArgumentException e) {

			prepareServiceForm(model, request, "edit", serviceId);

			model.addAttribute("errorMsg", e.getMessage());

			return "front-end/service/memberServiceForm";

		} catch (IOException e) {

			prepareServiceForm(model, request, "edit", serviceId);

			model.addAttribute("errorMsg", "圖片讀取失敗，請重新選擇圖片");

			return "front-end/service/memberServiceForm";
		}
	}

	@PostMapping("/{serviceId}/deactivate")
	public String deactivateService(@PathVariable Integer serviceId, HttpSession session,
			RedirectAttributes redirectAttributes) {

		Integer loginMemberId = getLoginMemberId(session);

		if (loginMemberId == null) {
			return LOGIN_URL;
		}

		try {
			serviceSvc.deactivateBySeller(serviceId, loginMemberId);

			redirectAttributes.addFlashAttribute("successMsg", "服務已下架");

		} catch (IllegalArgumentException e) {

			redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
		}

		return SERVICE_LIST_URL;
	}

	@PostMapping("/{serviceId}/activate")
	public String activateService(@PathVariable Integer serviceId, HttpSession session,
			RedirectAttributes redirectAttributes) {

		Integer loginMemberId = getLoginMemberId(session);

		if (loginMemberId == null) {
			return LOGIN_URL;
		}

		try {
			serviceSvc.activateBySeller(serviceId, loginMemberId);

			redirectAttributes.addFlashAttribute("successMsg", "服務已重新上架");

		} catch (IllegalArgumentException e) {

			redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
		}

		return SERVICE_LIST_URL;
	}

	@PostMapping("/{serviceId}/delete")
	public String deleteService(@PathVariable Integer serviceId, HttpSession session,
			RedirectAttributes redirectAttributes) {

		Integer loginMemberId = getLoginMemberId(session);

		if (loginMemberId == null) {
			return LOGIN_URL;
		}

		try {
			serviceSvc.deleteBySeller(serviceId, loginMemberId);

			redirectAttributes.addFlashAttribute("successMsg", "服務已刪除或封存");

		} catch (IllegalArgumentException e) {

			redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
		}

		return SERVICE_LIST_URL;
	}

	@GetMapping("/{serviceId}/slots")
	public String showSlotList(@PathVariable Integer serviceId, HttpSession session, Model model,
			RedirectAttributes redirectAttributes) {

		Integer loginMemberId = getLoginMemberId(session);

		if (loginMemberId == null) {
			return LOGIN_URL;
		}

		try {
			ServiceVO serviceVO = serviceSvc.getOwnServiceForEdit(serviceId, loginMemberId);

			List<ServiceSlotVO> slotList = serviceSlotSvc.getSlotsBySeller(serviceId, loginMemberId);

			model.addAttribute("serviceId", serviceId);
			model.addAttribute("serviceVO", serviceVO);
			model.addAttribute("slotList", slotList);

			return "front-end/service/memberServiceSlotList";

		} catch (IllegalArgumentException e) {

			redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());

			return SERVICE_LIST_URL;
		}
	}

	@PostMapping("/{serviceId}/slots")
	public String addSlots(@PathVariable Integer serviceId,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate slotDate,
			@RequestParam @DateTimeFormat(pattern = "HH:mm") LocalTime startTime,
			@RequestParam @DateTimeFormat(pattern = "HH:mm") LocalTime endTime,
			@RequestParam(defaultValue = "0") Integer endDayOffset, @RequestParam Integer splitMinutes,
			HttpSession session, RedirectAttributes redirectAttributes) {

		Integer loginMemberId = getLoginMemberId(session);

		if (loginMemberId == null) {
			return LOGIN_URL;
		}

		try {
			serviceSlotSvc.addSlotsBySeller(serviceId, loginMemberId, slotDate, startTime, endTime, endDayOffset,
					splitMinutes);

			redirectAttributes.addFlashAttribute("successMsg", "新增時段成功");

		} catch (IllegalArgumentException e) {

			redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
		}

		return redirectToSlotList(serviceId);
	}

	@PostMapping("/{serviceId}/slots/{serviceSlotId}/delete")
	public String deleteSlot(@PathVariable Integer serviceId, @PathVariable Integer serviceSlotId, HttpSession session,
			RedirectAttributes redirectAttributes) {

		Integer loginMemberId = getLoginMemberId(session);

		if (loginMemberId == null) {
			return LOGIN_URL;
		}

		try {
			serviceSlotSvc.deleteSlotBySeller(serviceId, serviceSlotId, loginMemberId);

			redirectAttributes.addFlashAttribute("successMsg", "刪除時段成功");

		} catch (IllegalArgumentException e) {

			redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
		}

		return redirectToSlotList(serviceId);
	}

	@PostMapping("/{serviceId}/slots/delete-available")
	public String deleteAvailableSlots(@PathVariable Integer serviceId, HttpSession session,
			RedirectAttributes redirectAttributes) {

		Integer loginMemberId = getLoginMemberId(session);

		if (loginMemberId == null) {
			return LOGIN_URL;
		}

		try {
			int deletedCount = serviceSlotSvc.deleteAvailableSlotsBySeller(serviceId, loginMemberId);

			redirectAttributes.addFlashAttribute("successMsg", "已清空 " + deletedCount + " 筆可預約時段");

		} catch (IllegalArgumentException e) {

			redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
		}

		return redirectToSlotList(serviceId);
	}

	private void prepareServiceForm(Model model, ServiceRequest request, String mode, Integer serviceId) {

		model.addAttribute("serviceRequest", request);

		model.addAttribute("serviceTypeList", serviceTypeSvc.getAll());

		model.addAttribute("mode", mode);

		if (serviceId != null) {
			model.addAttribute("serviceId", serviceId);
		}
	}

	private byte[] getImageBytes(MultipartFile imageFile) throws IOException {

		if (imageFile == null || imageFile.isEmpty()) {
			return null;
		}

		validateImageType(imageFile);

		return imageFile.getBytes();
	}

	private String getImageContentType(MultipartFile imageFile) {

		if (imageFile == null || imageFile.isEmpty()) {
			return null;
		}

		return imageFile.getContentType();
	}

	private void validateImageType(MultipartFile imageFile) {

		String contentType = imageFile.getContentType();

		boolean supported = "image/jpeg".equals(contentType) || "image/png".equals(contentType)
				|| "image/webp".equals(contentType) || "image/gif".equals(contentType);

		if (!supported) {
			throw new IllegalArgumentException("圖片格式只支援 JPG、PNG、WEBP、GIF");
		}
	}

	private Integer getLoginMemberId(HttpSession session) {

		Object sessionObject = session.getAttribute("memberVO");

		if (!(sessionObject instanceof MemberVO memberVO)) {
			return null;
		}

		return memberVO.getMemberId();
	}

	private String redirectToSlotList(Integer serviceId) {

		return "redirect:/member/services/" + serviceId + "/slots";
	}
}