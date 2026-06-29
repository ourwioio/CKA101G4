package com.webond.activity.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.webond.activity.model.ActivityOrderVO;

import java.util.List;

public interface ActivityOrderRepository extends JpaRepository<ActivityOrderVO, Integer> {
	List<ActivityOrderVO> findByActivityId(Integer activityId);

	List<ActivityOrderVO> findByMemberId(Integer memberId);
}