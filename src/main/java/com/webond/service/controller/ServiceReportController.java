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
import com.webond.employee.repository.EmployeeRepository;
import com.webond.service.model.ServiceReportVO;
import com.webond.service.service.ServiceReportService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/admin/serviceReport")
public class ServiceReportController {
	
	@Autowired
	ServiceReportService serviceReportService;
	
	@Autowired
	EmployeeRepository employeeRepository;
	

    private static final String EMPLOYEE_SESSION_KEY =
            "employeeVO";
    

    private EmployeeVO getLoginEmployeeVO(
            HttpSession session) {

        Object sessionObject =
                session.getAttribute(
                        EMPLOYEE_SESSION_KEY
                );

        if (sessionObject instanceof EmployeeVO employeeVO) {
            return employeeVO;
        }

        return null;
    }

    private Integer getLoginEmployeeId(
            HttpSession session) {

        EmployeeVO employeeVO =
                getLoginEmployeeVO(session);

        if (employeeVO == null) {
            return null;
        }

        return employeeVO.getEmployeeId();
    }


	
	@GetMapping("/listAllServiceReport")
	public String listAllServiceReport(
	        @RequestParam(required = false) Byte status,
	        ModelMap model,HttpSession session) {
        Integer loginEmployeeId = getLoginEmployeeId(session);
	  
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
	
	@PostMapping("/getOne_For_update")
	public String getOneServiceReport(@RequestParam("serviceReportId") Integer serviceReportId, ModelMap model,HttpSession session) {

        Integer loginEmployeeId =
                getLoginEmployeeId(session);
	  
		
		ServiceReportVO serviceReportVO = serviceReportService.getOneServiceReport(serviceReportId);
		model.addAttribute("serviceReportVO", serviceReportVO);
		
		return "back-end/service/serviceReportUpdate";
	}
	
	@PostMapping("/update")
	public String update(@Valid ServiceReportVO serviceReportVO, BindingResult result, ModelMap model, HttpSession session) {
	    Integer loginEmployeeId = getLoginEmployeeId(session);


	  
		try {
			serviceReportService.updateServiceReport(serviceReportVO);
		} catch (IllegalStateException e) {
			model.addAttribute("errorMessage", e.getMessage());
			ServiceReportVO current = serviceReportService.getOneServiceReport(serviceReportVO.getServiceReportId());
			model.addAttribute("serviceReportVO", current);
			return "back-end/service/serviceReportUpdate";
		}
		
		model.addAttribute("success", "finish");
		
		return "redirect:/admin/serviceReport/listAllServiceReport";
	}
	

	
}
