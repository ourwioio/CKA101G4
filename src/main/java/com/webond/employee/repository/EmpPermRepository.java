package com.webond.employee.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.webond.employee.model.EmpPermVO;

public interface EmpPermRepository extends JpaRepository<EmpPermVO, Integer>{

	@Modifying
	@Transactional
	@Query("DELETE FROM EmpPermVO e WHERE e.emps.employeeId = :empId")
	void deleteByEmployeeId(@Param("empId") Integer empId);
	
	@Modifying
	@Transactional
	@Query("DELETE FROM EmpPermVO e WHERE e.perms.permId = :permId")
	void deleteByPermId(@Param("permId") Integer permId);
	
}
