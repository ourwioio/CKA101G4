package com.webond.service.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.webond.employee.model.EmployeeVO;
import com.webond.service.model.ServiceReportVO;
import com.webond.service.service.ServiceReportService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/serviceReport")
public class ServiceReportController {
	
	@Autowired
	ServiceReportService serviceReportService;
	
	@GetMapping("/fakeLogin")
	public String fakeLogin(HttpSession session) {
		session.setAttribute("loginEmployeeId", 1001);
		return "back-end/member/empPage";
	}
	
	@GetMapping("/listAllServiceReport")
	public String listAllServiceReport(
	        @RequestParam(required = false) Byte status,
	        ModelMap model) {
		List<ServiceReportVO> list;
		
	    if (status == null) {
	        list = serviceReportService.getAll();
	    } else {
	        list = serviceReportService.getByStatus(status);
	    }
	    
		model.addAttribute("serviceReportListData", list);
	    model.addAttribute("currentStatus", status);
	    
		return "back-end/service/serviceReportList";
	}
	
	@PostMapping("getOne_For_update")
	public String getOneServiceReport(@RequestParam("serviceReportId") Integer serviceReportId, ModelMap model,HttpSession session) {

		Integer employeeId = (Integer) session.getAttribute("loginEmployeeId");
		if (employeeId == null) {
			return "redirect:/serviceReport/fakeLogin";
		}
		
		ServiceReportVO serviceReportVO = serviceReportService.getOneServiceReport(serviceReportId);
		model.addAttribute("serviceReportVO", serviceReportVO);
		
		return "back-end/service/serviceReportUpdate";
	}
	
	@PostMapping("update")
	public String update(@Valid ServiceReportVO serviceReportVO, BindingResult result, ModelMap model, HttpSession session) {

		Integer employeeId = (Integer) session.getAttribute("loginEmployeeId");
		if (employeeId == null) {
			return "redirect:/serviceReport/fakeLogin";
		}

	    EmployeeVO employee = new EmployeeVO();
	    employee.setEmployeeId(employeeId);
	    serviceReportVO.setEmployee(employee);
	  
		try {
			serviceReportService.updateServiceReport(serviceReportVO);
		} catch (IllegalStateException e) {
			model.addAttribute("errorMessage", e.getMessage());
			ServiceReportVO current = serviceReportService.getOneServiceReport(serviceReportVO.getServiceReportId());
			model.addAttribute("serviceReportVO", current);
			return "back-end/service/serviceReportUpdate";
		}
		
		model.addAttribute("success", "finish");
		
		return "redirect:/serviceReport/listAllServiceReport";
	}
	

	
}
