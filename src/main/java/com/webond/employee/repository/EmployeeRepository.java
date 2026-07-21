package com.webond.employee.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

	// 忘記密碼用
	Optional<EmployeeVO> findByResetToken(String resetToken);

	// 判斷新增時帳號存不存在
	boolean existsByEmpAccount(String empAccount);

	// 給最後登入時間用
	@Modifying
	@Transactional
	@Query("UPDATE EmployeeVO e SET e.lastLoginAt = :now WHERE e.employeeId = :id")
	void updateLastLoginAt(@Param("id") Integer id, @Param("now") LocalDateTime now);

	@Modifying
	@Transactional
	@Query("UPDATE EmployeeVO e SET e.resetToken = :token, e.tokenExpiry = :expiry WHERE e.employeeId = :id")
	void updateResetTokenOnly(@Param("id") Integer id, @Param("token") String token,
			@Param("expiry") LocalDateTime expiry);

	// 局部更新密碼並清除 Token，狀態設為 1 (啟用)
	@Modifying
	@Transactional
	@Query("UPDATE EmployeeVO e SET e.empPassword = :password, e.empStatus = 1, e.resetToken = null, e.tokenExpiry = null WHERE e.resetToken = :token")
	int updatePasswordAndClearToken(@Param("token") String token, @Param("password") String password);

	@Query("SELECT DISTINCT e FROM EmployeeVO e JOIN e.empPermVO p WHERE p.perms.permId = 3")
	List<EmployeeVO> findEmployeesWithActRptPermission();
	
	
	@Query("SELECT e FROM EmployeeVO e WHERE " +
	           "(:empName IS NULL OR LENGTH(TRIM(:empName)) = 0 OR e.empName LIKE CONCAT('%', :empName, '%')) AND " +
	           "(:roleTitle IS NULL OR e.roleTitle = :roleTitle) AND " +
	           "(:empStatus IS NULL OR e.empStatus = :empStatus)")
    Page<EmployeeVO> findByCompositeSearch(
        @Param("empName") String empName,
        @Param("roleTitle") Integer roleTitle,
        @Param("empStatus") Integer empStatus,
        Pageable pageable
    );
	
    @Modifying
    @Transactional
    @Query("DELETE FROM EmployeeVO e WHERE e.employeeId = :employeeId")
    void deleteEmployeeDirectly(@Param("employeeId") Integer employeeId);
}