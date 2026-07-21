package com.webond.activity.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.webond.activity.model.ActRptVO;

@Repository
public interface ActRptRepository extends JpaRepository<ActRptVO, Integer>{
	
	
	@Query("SELECT r FROM ActRptVO r WHERE " +
	           "(:status IS NULL OR r.actRptStatus = :status) AND " +
	           "(:empId IS NULL OR r.empId.employeeId = :empId) AND "  +
	           "(:rptType IS NULL OR r.rptType = :rptType)")
	    Page<ActRptVO> findByCompositeSearch(
	        @Param("status") Integer status, 
	        @Param("empId") Integer empId,
	        @Param("rptType") Integer rptType,
	        Pageable pageable
	    );
	
}
