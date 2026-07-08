package com.webond.employee.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import com.webond.employee.model.EmpPermVO;
import com.webond.employee.model.EmployeeVO;

public interface EmpPermRepository extends JpaRepository<EmpPermVO, Integer>{

	@Transactional
	void deleteByEmps(EmployeeVO emps);
	
}
