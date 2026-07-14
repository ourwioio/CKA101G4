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
import com.webond.service.model.ServiceReportVO;
import com.webond.service.repository.ServiceRepository;
import com.webond.service.service.ServiceReportService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
@RequestMapping("front/serviceReport")
public class ServiceReportFrontController {

    @Autowired
    private ServiceReportService serviceReportService;
    

    @GetMapping("/add")
    public String addServiceReport(@RequestParam("serviceId") Integer serviceId,
                                  ModelMap model,HttpSession session) {
		
		MemberVO loginMember = (MemberVO) session.getAttribute("memberVO");
		if(loginMember == null) {
			return "redirect:/member/login";
		}
	    
	    ServiceReportVO serviceReportVO = new ServiceReportVO();

	    model.addAttribute("serviceReportVO", serviceReportVO);
	    model.addAttribute("serviceId", serviceId);
	    return "front-end/service/serviceReportFront";
    }
    
    
    @PostMapping("/insert")
    public String submit(@RequestParam("serviceId") Integer serviceId,
                         @Valid @ModelAttribute("serviceReportVO") ServiceReportVO serviceReportVO,
                         BindingResult result,
                         HttpSession session,
                         ModelMap model) {   

        MemberVO loginMember = (MemberVO) session.getAttribute("memberVO");
        if (loginMember == null) {
            return "redirect:/member/login";
        }
        serviceReportVO.setReporterMember(loginMember);

        try {
            serviceReportService.checkCanReport(serviceId);
        } catch (IllegalArgumentException | IllegalStateException e) {
            model.addAttribute("serviceError", e.getMessage());
            model.addAttribute("serviceId", serviceId); 
            return "front-end/service/serviceList";
        }

        serviceReportService.submitReport(serviceReportVO, serviceId);
        model.addAttribute("success", "finish");

        return "redirect:/front/serviceReport/success";
    } 
    
    @GetMapping("/success")
    public String reportSuccess() {
        return "front-end/service/serviceReportSuccess";
    }

}