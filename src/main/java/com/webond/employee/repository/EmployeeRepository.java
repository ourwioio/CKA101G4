package com.webond.employee.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import com.webond.employee.model.EmployeeVO;

// 🎯 確保後面這兩個地方是 EmployeeVO 和 Integer！
public interface EmployeeRepository extends JpaRepository<EmployeeVO, Integer> {

	// 給管理員Service的
	Optional<EmployeeVO> findByEmpAccount(String empAccount);
}