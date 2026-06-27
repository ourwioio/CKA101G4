package com.webond.service.controller;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.webond.service.dto.ServiceRequest;
import com.webond.service.model.ServiceService;
import com.webond.service.model.ServiceTypeService;
import com.webond.service.model.ServiceVO;

@Controller
@RequestMapping("/services")
public class ServiceController {

    private final ServiceService serviceSvc;
    private final ServiceTypeService serviceTypeSvc;

    public ServiceController(ServiceService serviceSvc, ServiceTypeService serviceTypeSvc) {
        this.serviceSvc = serviceSvc;
        this.serviceTypeSvc = serviceTypeSvc;
    }

    // 查全部服務
    // URL: GET /services
    @GetMapping
    public String getAllServices(Model model) {

        List<ServiceVO> serviceList = serviceSvc.getAll();

        model.addAttribute("serviceList", serviceList);
        model.addAttribute("serviceTypeList", serviceTypeSvc.getAll());

        return "service/list";
    }

    // 查單一服務
    // URL: GET /services/{serviceId}
    @GetMapping("/{serviceId}")
    public String getOneService(@PathVariable Integer serviceId, Model model) {

        ServiceVO serviceVO = serviceSvc.getOneService(serviceId);

        if (serviceVO == null) {
            model.addAttribute("errorMsg", "查無此服務");
            model.addAttribute("serviceList", serviceSvc.getAll());
            model.addAttribute("serviceTypeList", serviceTypeSvc.getAll());
            return "service/list";
        }

        model.addAttribute("serviceVO", serviceVO);

        return "service/detail";
    }

    // 依服務類型查服務
    // URL: GET /services/by-type/{serviceTypeId}
    @GetMapping("/by-type/{serviceTypeId}")
    public String getServicesByType(@PathVariable Integer serviceTypeId, Model model) {

        List<ServiceVO> serviceList = serviceSvc.getServicesByServiceTypeId(serviceTypeId);

        model.addAttribute("serviceList", serviceList);
        model.addAttribute("serviceTypeList", serviceTypeSvc.getAll());
        model.addAttribute("selectedServiceTypeId", serviceTypeId);

        return "service/list";
    }

    // 前往新增服務頁面
    // URL: GET /services/new
    @GetMapping("/new")
    public String showAddForm(Model model) {

        model.addAttribute("serviceRequest", new ServiceRequest());
        model.addAttribute("serviceTypeList", serviceTypeSvc.getAll());
        model.addAttribute("mode", "add");

        return "service/form";
    }

    // 新增服務
    // URL: POST /services
    @PostMapping
    public String insertService(
            @ModelAttribute ServiceRequest request,
            Model model,
            RedirectAttributes redirectAttributes) {

        Map<String, String> errors = validateServiceRequest(request);

        if (!errors.isEmpty()) {
            model.addAttribute("errors", errors);
            model.addAttribute("serviceRequest", request);
            model.addAttribute("serviceTypeList", serviceTypeSvc.getAll());
            model.addAttribute("mode", "add");

            return "service/form";
        }

        serviceSvc.add(
                request.getServiceTypeId(),
                request.getMemberId(),
                request.getServiceName().trim(),
                request.getDescription().trim(),
                request.getHourlyRate(),
                request.getStatus().byteValue(),
                LocalDateTime.now()
        );

        redirectAttributes.addFlashAttribute("successMsg", "新增服務成功");

        return "redirect:/services";
    }

    // 前往修改服務頁面
    // URL: GET /services/{serviceId}/edit
    @GetMapping("/{serviceId}/edit")
    public String showUpdateForm(@PathVariable Integer serviceId, Model model) {

        ServiceVO serviceVO = serviceSvc.getOneService(serviceId);

        if (serviceVO == null) {
            model.addAttribute("errorMsg", "查無此服務，無法修改");
            model.addAttribute("serviceList", serviceSvc.getAll());
            model.addAttribute("serviceTypeList", serviceTypeSvc.getAll());

            return "service/list";
        }

        ServiceRequest request = toServiceRequest(serviceVO);

        model.addAttribute("serviceId", serviceId);
        model.addAttribute("serviceRequest", request);
        model.addAttribute("serviceTypeList", serviceTypeSvc.getAll());
        model.addAttribute("mode", "edit");

        return "service/form";
    }

    // 修改服務
    // URL: POST /services/{serviceId}/edit
    @PostMapping("/{serviceId}/edit")
    public String updateService(
            @PathVariable Integer serviceId,
            @ModelAttribute ServiceRequest request,
            Model model,
            RedirectAttributes redirectAttributes) {

        Map<String, String> errors = validateServiceRequest(request);

        if (!errors.isEmpty()) {
            model.addAttribute("errors", errors);
            model.addAttribute("serviceId", serviceId);
            model.addAttribute("serviceRequest", request);
            model.addAttribute("serviceTypeList", serviceTypeSvc.getAll());
            model.addAttribute("mode", "edit");

            return "service/form";
        }

        ServiceVO oldService = serviceSvc.getOneService(serviceId);

        if (oldService == null) {
            model.addAttribute("errorMsg", "查無此服務，無法修改");
            model.addAttribute("serviceList", serviceSvc.getAll());
            model.addAttribute("serviceTypeList", serviceTypeSvc.getAll());

            return "service/list";
        }

        serviceSvc.update(
                serviceId,
                request.getServiceTypeId(),
                request.getMemberId(),
                request.getServiceName().trim(),
                request.getDescription().trim(),
                request.getHourlyRate(),
                request.getStatus().byteValue()
        );

        redirectAttributes.addFlashAttribute("successMsg", "修改服務成功");

        return "redirect:/services/" + serviceId;
    }

    // 刪除服務
    // URL: POST /services/{serviceId}/delete
    @PostMapping("/{serviceId}/delete")
    public String deleteService(
            @PathVariable Integer serviceId,
            RedirectAttributes redirectAttributes) {

        ServiceVO oldService = serviceSvc.getOneService(serviceId);

        if (oldService == null) {
            redirectAttributes.addFlashAttribute("errorMsg", "查無此服務，無法刪除");
            return "redirect:/services";
        }

        serviceSvc.delete(serviceId);

        redirectAttributes.addFlashAttribute("successMsg", "刪除服務成功");

        return "redirect:/services";
    }

    // 把 ServiceVO 轉成 ServiceRequest，給修改表單回填用
    private ServiceRequest toServiceRequest(ServiceVO serviceVO) {
        ServiceRequest request = new ServiceRequest();

        request.setServiceTypeId(serviceVO.getServiceTypeId());
        request.setMemberId(serviceVO.getMemberId());
        request.setServiceName(serviceVO.getServiceName());
        request.setDescription(serviceVO.getDescription());
        request.setHourlyRate(serviceVO.getHourlyRate());

        if (serviceVO.getStatus() != null) {
            request.setStatus(serviceVO.getStatus().intValue());
        }

        return request;
    }

    // 表單驗證
    private Map<String, String> validateServiceRequest(ServiceRequest request) {
        Map<String, String> errors = new LinkedHashMap<>();

        if (request == null) {
            errors.put("request", "請提供服務資料");
            return errors;
        }

        if (request.getServiceTypeId() == null || request.getServiceTypeId() <= 0) {
            errors.put("serviceTypeId", "請選擇服務類型");
        }

        if (request.getMemberId() == null || request.getMemberId() <= 0) {
            errors.put("memberId", "會員編號必須大於 0");
        }

        if (request.getServiceName() == null || request.getServiceName().trim().isEmpty()) {
            errors.put("serviceName", "服務名稱請勿空白");
        } else if (request.getServiceName().trim().length() > 50) {
            errors.put("serviceName", "服務名稱不可超過 50 個字");
        }

        if (request.getDescription() == null || request.getDescription().trim().isEmpty()) {
            errors.put("description", "服務描述請勿空白");
        }

        if (request.getHourlyRate() == null) {
            errors.put("hourlyRate", "每小時費率必須是整數");
        } else if (request.getHourlyRate() < 0) {
            errors.put("hourlyRate", "每小時費率不可小於 0");
        }

        if (request.getStatus() == null) {
            errors.put("status", "請選擇服務狀態");
        } else if (request.getStatus() != 0 && request.getStatus() != 1) {
            errors.put("status", "服務狀態只能是 0 或 1");
        }

        return errors;
    }
}