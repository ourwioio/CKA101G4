package com.activity.model;

import org.springframework.data.jpa.repository.JpaRepository;
import com.activity.model.ActivityOrderVO;
import java.util.List;

public interface ActivityOrderRepository extends JpaRepository<ActivityOrderVO, Integer> {
	List<ActivityOrderVO> findByActivityId(Integer activityId);

	List<ActivityOrderVO> findByMemberId(Integer memberId);
}