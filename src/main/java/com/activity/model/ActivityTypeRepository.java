package com.activity.model;

import org.springframework.data.jpa.repository.JpaRepository;
import com.activity.model.ActivityTypeVO;

public interface ActivityTypeRepository extends JpaRepository<ActivityTypeVO, Integer> {
}