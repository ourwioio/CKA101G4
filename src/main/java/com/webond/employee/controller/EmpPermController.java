package com.webond.employee.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.webond.employee.model.EmpService;
import com.webond.employee.model.PermissionService;
import com.webond.employee.model.PermissionVO;

@Controller
@RequestMapping("/admin")
public class EmpPermController {
	
	@Autowired
	EmpService empSvc;
	
	@Autowired
	PermissionService permSvc;
	


}
