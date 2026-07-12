package com.webond.employee.model;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.webond.employee.repository.EmpPermRepository;

@Service
public class EmpPermService {
	
	@Autowired
	private EmpPermRepository empPermRepo;

	

	
	
}
