package com.activity.model;

import org.springframework.data.jpa.repository.JpaRepository;
import com.activity.model.ActivityVO;

public interface ActivityRepository extends JpaRepository<ActivityVO, Integer> {
}