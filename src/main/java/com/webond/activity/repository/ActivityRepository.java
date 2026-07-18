package com.webond.activity.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.webond.activity.model.ActivityVO;

public interface ActivityRepository extends JpaRepository<ActivityVO, Integer>, JpaSpecificationExecutor<ActivityVO> {

	List<ActivityVO> findByMemberId(Integer memberId);

	List<ActivityVO> findByMemberIdAndEndTimeAfter(Integer memberId, LocalDateTime endTime);

}
