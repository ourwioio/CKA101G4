package com.webond.service.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.webond.service.model.ServiceVO;
import com.webond.service.service.ServiceService;

@Controller
@RequestMapping("/admin/services")
public class AdminServiceController {

    private final ServiceService serviceSvc;

    public AdminServiceController(ServiceService serviceSvc) {
        this.serviceSvc = serviceSvc;
    }

    // 後台：查看全部服務 
    // URL: GET /admin/services
    @GetMapping
    public String listAllServices(Model model) {

        List<ServiceVO> serviceList = serviceSvc.getAll();

        model.addAttribute("serviceList", serviceList);

        return "back-end/service/adminServiceList";
    }

    // 後台：查看單一服務詳情
    // URL: GET /admin/services/{serviceId}
    @GetMapping("/{serviceId}")
    public String getServiceDetail(@PathVariable Integer serviceId,
                                   Model model,
                                   RedirectAttributes redirectAttributes) {

        ServiceVO serviceVO = serviceSvc.getOneService(serviceId);

        if (serviceVO == null) {
            redirectAttributes.addFlashAttribute("errorMsg", "查無此服務");
            return "redirect:/admin/services";
        }

        model.addAttribute("serviceVO", serviceVO);

        return "back-end/service/adminServiceDetail";
    }

    // 後台：平台停用服務
    // URL: POST /admin/services/{serviceId}/disable
    @PostMapping("/{serviceId}/disable")
    public String disableService(@PathVariable Integer serviceId,
                                 RedirectAttributes redirectAttributes) {

        try {
            serviceSvc.disableByAdmin(serviceId);
            redirectAttributes.addFlashAttribute("successMsg", "服務已由平台停用");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
        }

        return "redirect:/admin/services";
    }

    // 後台：恢復服務
    // URL: POST /admin/services/{serviceId}/restore
    @PostMapping("/{serviceId}/restore")
    public String restoreService(@PathVariable Integer serviceId,
                                 RedirectAttributes redirectAttributes) {

        try {
            serviceSvc.restoreByAdmin(serviceId);
            redirectAttributes.addFlashAttribute("successMsg", "服務已恢復上架");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
        }

        return "redirect:/admin/services";
    }
}