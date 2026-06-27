package com.webond.service.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.webond.service.model.ServiceService;
import com.webond.service.model.ServiceSlotService;
import com.webond.service.model.ServiceTypeService;

@Controller
public class ServiceAdminController {

    private final ServiceService serviceSvc;
    private final ServiceTypeService serviceTypeSvc;
    private final ServiceSlotService serviceSlotSvc;

    public ServiceAdminController(ServiceService serviceSvc,
                                  ServiceTypeService serviceTypeSvc,
                                  ServiceSlotService serviceSlotSvc) {
        this.serviceSvc = serviceSvc;
        this.serviceTypeSvc = serviceTypeSvc;
        this.serviceSlotSvc = serviceSlotSvc;
    }

    @GetMapping("/admin/services")
    public String serviceAdminPage(Model model) {
        model.addAttribute("serviceList", serviceSvc.getAll());
        model.addAttribute("serviceTypeList", serviceTypeSvc.getAll());
        model.addAttribute("serviceSlotList", serviceSlotSvc.getAll());

        return "back-end/service/index";
    }
}