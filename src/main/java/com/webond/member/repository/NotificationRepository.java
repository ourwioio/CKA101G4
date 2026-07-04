package com.webond.member.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.webond.member.model.NotificationVO;

public interface NotificationRepository  extends JpaRepository<NotificationVO, Integer> {
	List<NotificationVO> findByMember_MemberId(Integer memberId);
	List<NotificationVO> findByEmployee_EmployeeId(Integer employeeId);
	
	@Modifying
	@Transactional
	@Query("UPDATE NotificationVO n SET n.isRead = 1 WHERE n.notificationId = :notificationId")
	void markAsRead(@Param("notificationId") Integer notificationId);

}
