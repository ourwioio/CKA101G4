package com.webond.service.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.webond.member.model.MemberVO;
import com.webond.service.model.ServiceOrderVO;
import com.webond.service.model.ServiceReportVO;
import com.webond.service.repository.ServiceOrderRepository;
import com.webond.service.service.ServiceReportService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
@RequestMapping("front/serviceReport")
public class ServiceReportFrontController {

    @Autowired
    private ServiceReportService serviceReportService;

    @Autowired
    private ServiceOrderRepository serviceOrderRepository;
    

    @GetMapping("/add")
    public String addServiceReport(@RequestParam("orderId") Integer orderId,
                                  ModelMap model,HttpSession session) {
		
		MemberVO loginMember = (MemberVO) session.getAttribute("memberVO");
		if(loginMember == null) {
			return "redirect:/member/login";
		}

	    ServiceOrderVO serviceOrderVO = serviceOrderRepository.findById(orderId).orElse(null);
	    if (serviceOrderVO == null) {
	        model.addAttribute("orderError", "訂單不存在");
	        return "redirect:/member/service-orders/buyer";
	    }

	    if (!serviceOrderVO.getBuyerMemberId().equals(loginMember.getMemberId())) {
	        return "redirect:/member/login";
	    }
	    
	    ServiceReportVO serviceReportVO = new ServiceReportVO();
	    serviceReportVO.setServiceOrder(serviceOrderVO);

	    try {
	        serviceReportService.checkCanReport(serviceReportVO);
	    } catch (IllegalArgumentException | IllegalStateException e) {
	        model.addAttribute("orderError", e.getMessage());
	        return "redirect:/member/service-orders/buyer";
	    }

	    model.addAttribute("serviceOrderVO", serviceOrderVO);
	    model.addAttribute("serviceReportVO", serviceReportVO);
	    return "front-end/service/serviceReportFront";
    }
    @PostMapping("/insert")
    public String submit(@RequestParam("orderId") Integer orderId,
                         @Valid @ModelAttribute("serviceReportVO") ServiceReportVO serviceReportVO,
                         BindingResult result,
                         HttpSession session,
                         ModelMap model,
                         RedirectAttributes redirectAttributes) {   // ← 新增這個參數

        MemberVO loginMember = (MemberVO) session.getAttribute("memberVO");
        if (loginMember == null) {
            return "redirect:/member/login";
        }

        ServiceOrderVO serviceOrderVO = serviceOrderRepository.findById(orderId).orElse(null);
        if (serviceOrderVO == null) {
            model.addAttribute("orderError", "訂單不存在");
            return "front-end/service/serviceReportFront";
        }

        if (!serviceOrderVO.getBuyerMemberId().equals(loginMember.getMemberId())) {
            return "redirect:/member/login";
        }

        serviceReportVO.setServiceOrder(serviceOrderVO);
        serviceReportVO.setReporterMember(loginMember);

        if (result.hasErrors()) {
            model.addAttribute("serviceOrderVO", serviceOrderVO);
            return "front-end/service/serviceReportFront";
        }

        try {
            serviceReportService.checkCanReport(serviceReportVO);
        } catch (IllegalArgumentException | IllegalStateException e) {
            model.addAttribute("serviceError", e.getMessage());
            model.addAttribute("serviceOrderVO", serviceOrderVO);
            return "front-end/service/serviceReportFront";
        }

        serviceReportService.submitReport(serviceReportVO);

        return "redirect:/front/serviceReport/success";
    } 
    
    @GetMapping("/success")
    public String reportSuccess() {
        return "front-end/service/serviceReportSuccess";
    }

}