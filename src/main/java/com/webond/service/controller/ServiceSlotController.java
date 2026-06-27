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

import com.webond.service.model.ServiceService;
import com.webond.service.model.ServiceSlotService;
import com.webond.service.model.ServiceSlotVO;

@Controller
@RequestMapping("/service-slots")
public class ServiceSlotController {

    private final ServiceSlotService serviceSlotSvc;
    private final ServiceService serviceSvc;

    public ServiceSlotController(ServiceSlotService serviceSlotSvc,
                                 ServiceService serviceSvc) {
        this.serviceSlotSvc = serviceSlotSvc;
        this.serviceSvc = serviceSvc;
    }

    // 查全部服務時段
    // URL: GET /service-slots
    @GetMapping
    public String getAll(Model model) {
        List<ServiceSlotVO> serviceSlotList = serviceSlotSvc.getAll();

        model.addAttribute("serviceSlotList", serviceSlotList);
        model.addAttribute("serviceList", serviceSvc.getAll());

        return "back-end/service/service-slot-list";
    }

    // 查單一服務時段
    // URL: GET /service-slots/{serviceSlotId}
    @GetMapping("/{serviceSlotId}")
    public String getOne(@PathVariable Integer serviceSlotId, Model model) {
        ServiceSlotVO serviceSlotVO = serviceSlotSvc.getOneServiceSlot(serviceSlotId);

        if (serviceSlotVO == null) {
            model.addAttribute("errorMsg", "查無此服務時段");
            model.addAttribute("serviceSlotList", serviceSlotSvc.getAll());
            model.addAttribute("serviceList", serviceSvc.getAll());

            return "back-end/service/service-slot-list";
        }

        model.addAttribute("serviceSlotVO", serviceSlotVO);

        return "back-end/service/service-slot-detail";
    }

    // 依服務編號查服務時段
    // URL: GET /service-slots/service/{serviceId}
    @GetMapping("/service/{serviceId}")
    public String getByServiceId(@PathVariable Integer serviceId, Model model) {
        List<ServiceSlotVO> serviceSlotList = serviceSlotSvc.getByServiceId(serviceId);

        model.addAttribute("serviceSlotList", serviceSlotList);
        model.addAttribute("serviceList", serviceSvc.getAll());
        model.addAttribute("selectedServiceId", serviceId);

        return "back-end/service/service-slot-list";
    }

    // 前往新增頁面
    // URL: GET /service-slots/new
    @GetMapping("/new")
    public String showAddForm(Model model) {
        model.addAttribute("serviceSlotVO", new ServiceSlotVO());
        model.addAttribute("serviceList", serviceSvc.getAll());
        model.addAttribute("mode", "add");

        return "back-end/service/service-slot-form";
    }

    // 新增服務時段
    // URL: POST /service-slots
    @PostMapping
    public String insert(
            @ModelAttribute ServiceSlotVO serviceSlotVO,
            Model model,
            RedirectAttributes redirectAttributes) {

        Map<String, String> errors = validateServiceSlot(serviceSlotVO);

        if (!errors.isEmpty()) {
            model.addAttribute("errors", errors);
            model.addAttribute("serviceSlotVO", serviceSlotVO);
            model.addAttribute("serviceList", serviceSvc.getAll());
            model.addAttribute("mode", "add");

            return "back-end/service/service-slot-form";
        }

        serviceSlotSvc.add(
                serviceSlotVO.getServiceId(),
                serviceSlotVO.getStartTime(),
                serviceSlotVO.getEndTime(),
                serviceSlotVO.getSlotStatus(),
                serviceSlotVO.getLockExpiresAt()
        );

        redirectAttributes.addFlashAttribute("successMsg", "新增服務時段成功");

        return "redirect:/service-slots";
    }

    // 前往修改頁面
    // URL: GET /service-slots/{serviceSlotId}/edit
    @GetMapping("/{serviceSlotId}/edit")
    public String showUpdateForm(@PathVariable Integer serviceSlotId, Model model) {
        ServiceSlotVO serviceSlotVO = serviceSlotSvc.getOneServiceSlot(serviceSlotId);

        if (serviceSlotVO == null) {
            model.addAttribute("errorMsg", "查無此服務時段，無法修改");
            model.addAttribute("serviceSlotList", serviceSlotSvc.getAll());
            model.addAttribute("serviceList", serviceSvc.getAll());

            return "back-end/service/service-slot-list";
        }

        model.addAttribute("serviceSlotVO", serviceSlotVO);
        model.addAttribute("serviceList", serviceSvc.getAll());
        model.addAttribute("mode", "edit");

        return "back-end/service/service-slot-form";
    }

    // 修改服務時段
    // URL: POST /service-slots/{serviceSlotId}/edit
    @PostMapping("/{serviceSlotId}/edit")
    public String update(
            @PathVariable Integer serviceSlotId,
            @ModelAttribute ServiceSlotVO serviceSlotVO,
            Model model,
            RedirectAttributes redirectAttributes) {

        Map<String, String> errors = validateServiceSlot(serviceSlotVO);

        if (!errors.isEmpty()) {
            serviceSlotVO.setServiceSlotId(serviceSlotId);

            model.addAttribute("errors", errors);
            model.addAttribute("serviceSlotVO", serviceSlotVO);
            model.addAttribute("serviceList", serviceSvc.getAll());
            model.addAttribute("mode", "edit");

            return "back-end/service/service-slot-form";
        }

        ServiceSlotVO oldVO = serviceSlotSvc.getOneServiceSlot(serviceSlotId);

        if (oldVO == null) {
            model.addAttribute("errorMsg", "查無此服務時段，無法修改");
            model.addAttribute("serviceSlotList", serviceSlotSvc.getAll());
            model.addAttribute("serviceList", serviceSvc.getAll());

            return "back-end/service/service-slot-list";
        }

        serviceSlotSvc.update(
                serviceSlotId,
                serviceSlotVO.getServiceId(),
                serviceSlotVO.getStartTime(),
                serviceSlotVO.getEndTime(),
                serviceSlotVO.getSlotStatus(),
                serviceSlotVO.getLockExpiresAt()
        );

        redirectAttributes.addFlashAttribute("successMsg", "修改服務時段成功");

        return "redirect:/service-slots/" + serviceSlotId;
    }

    // 刪除服務時段
    // URL: POST /service-slots/{serviceSlotId}/delete
    @PostMapping("/{serviceSlotId}/delete")
    public String delete(
            @PathVariable Integer serviceSlotId,
            RedirectAttributes redirectAttributes) {

        ServiceSlotVO oldVO = serviceSlotSvc.getOneServiceSlot(serviceSlotId);

        if (oldVO == null) {
            redirectAttributes.addFlashAttribute("errorMsg", "查無此服務時段，無法刪除");
            return "redirect:/service-slots";
        }

        serviceSlotSvc.delete(serviceSlotId);

        redirectAttributes.addFlashAttribute("successMsg", "刪除服務時段成功");

        return "redirect:/service-slots";
    }

    private Map<String, String> validateServiceSlot(ServiceSlotVO vo) {
        Map<String, String> errors = new LinkedHashMap<>();

        if (vo == null) {
            errors.put("request", "請提供服務時段資料");
            return errors;
        }

        if (vo.getServiceId() == null || vo.getServiceId() <= 0) {
            errors.put("serviceId", "請選擇服務");
        }

        LocalDateTime startTime = vo.getStartTime();
        LocalDateTime endTime = vo.getEndTime();

        if (startTime == null) {
            errors.put("startTime", "開始時間請勿空白");
        }

        if (endTime == null) {
            errors.put("endTime", "結束時間請勿空白");
        }

        if (startTime != null && endTime != null && !endTime.isAfter(startTime)) {
            errors.put("endTime", "結束時間必須晚於開始時間");
        }

        if (vo.getSlotStatus() == null) {
            errors.put("slotStatus", "請選擇時段狀態");
        } else if (vo.getSlotStatus() < 0 || vo.getSlotStatus() > 2) {
            errors.put("slotStatus", "時段狀態只能是 0、1、2");
        }

        return errors;
    }
}