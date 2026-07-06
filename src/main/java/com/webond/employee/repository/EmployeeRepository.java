package com.webond.employee.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import com.webond.employee.model.EmployeeVO;

// 🎯 確保後面這兩個地方是 EmployeeVO 和 Integer！
public interface EmployeeRepository extends JpaRepository<EmployeeVO, Integer> {

	// 給adminService的
	Optional<EmployeeVO> findByEmpAccount(String empAccount);
	
	// 判斷新增時帳號存不存在
	boolean existsByEmpAccount(String empAccount);
}