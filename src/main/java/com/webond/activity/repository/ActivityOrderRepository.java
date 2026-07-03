package com.webond.activity.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.webond.activity.model.ActivityOrderVO;

public interface ActivityOrderRepository extends JpaRepository<ActivityOrderVO, Integer> {

	List<ActivityOrderVO> findByActivityId(Integer activityId);

	List<ActivityOrderVO> findByBuyerMemberId(Integer buyerMemberId);

}