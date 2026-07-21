package com.webond.employee.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.webond.employee.model.PermissionVO;

public interface PermissionRepository extends JpaRepository<PermissionVO, Integer>{

	 @Query("SELECT p FROM PermissionVO p WHERE " +
	           "(:permName IS NULL OR LENGTH(TRIM(:permName)) = 0 OR p.permName LIKE CONCAT('%', :permName, '%')) AND " +
	           "(:hasEmp IS NULL OR " +
	           " (:hasEmp = true AND p.empPermVO IS NOT EMPTY) OR " +
	           " (:hasEmp = false AND p.empPermVO IS EMPTY))")
	    Page<PermissionVO> findByCompositeSearch(
	        @Param("permName") String permName,
	        @Param("hasEmp") Boolean hasEmp, 
	        Pageable pageable
	    );
}
