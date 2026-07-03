package com.webond.employee.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.webond.employee.model.EmployeeVO;

// 🎯 確保後面這兩個地方是 EmployeeVO 和 Integer！
public interface EmployeeRepository extends JpaRepository<EmployeeVO, Integer> {
}