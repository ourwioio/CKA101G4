package com.webond.employee.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.webond.employee.model.EmployeeVO;

// 🎯 確保後面這兩個地方是 EmployeeVO 和 Integer！
public interface EmployeeRepository extends JpaRepository<EmployeeVO, Integer> {

	// 給adminService的
	Optional<EmployeeVO> findByEmpAccount(String empAccount);
	
	// 判斷新增時帳號存不存在
	boolean existsByEmpAccount(String empAccount);
	
	// 給最後登入時間用
	@Modifying
	@Transactional
	@Query("UPDATE EmployeeVO e SET e.lastLoginAt = :now WHERE e.employeeId = :id")
	void updateLastLoginAt(@Param("id") Integer id, @Param("now") LocalDateTime now);

}