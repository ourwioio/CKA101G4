package com.webond.employee.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.webond.employee.model.EmployeeVO;

/**
 * 只要繼承 JpaRepository<實體類別, 主鍵型別>,就「自動」擁有:
 *   findAll()、findById()、save()、delete()、count()... 等一整套方法,
 *   「一行實作都不用寫」。
 *
 * 還能用「方法名稱」自動生成查詢,例如宣告:
 *   List<Emp> findByJob(String job);      ← 不用寫實作,Spring Data 自動依方法名產生查詢
 *   List<Emp> findByEname(String ename);
 * Spring Data JPA 會解析方法名稱、自動產生對應的 SQL。
 */

public interface EmpRepository extends JpaRepository<EmployeeVO, Integer>{
	// 不需要寫任何東西! findAll() 等方法已內建。
	
	// 給管理員Service的
	Optional<EmployeeVO> findByEmpAccount(String empAccount);

}
