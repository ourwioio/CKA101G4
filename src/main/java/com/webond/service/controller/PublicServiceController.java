package com.webond.service.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.webond.service.model.ServiceSlotVO;
import com.webond.service.model.ServiceTypeVO;
import com.webond.service.model.ServiceVO;
import com.webond.service.service.ServiceService;
import com.webond.service.service.ServiceSlotService;
import com.webond.service.service.ServiceTypeService;

@Controller
@RequestMapping("/front/services")
public class PublicServiceController {

    private final ServiceService serviceSvc;
    private final ServiceTypeService serviceTypeSvc;
    private final ServiceSlotService serviceSlotSvc;

    public PublicServiceController(ServiceService serviceSvc,
                                   ServiceTypeService serviceTypeSvc,
                                   ServiceSlotService serviceSlotSvc) {
        this.serviceSvc = serviceSvc;
        this.serviceTypeSvc = serviceTypeSvc;
        this.serviceSlotSvc = serviceSlotSvc;
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
}