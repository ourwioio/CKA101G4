package com.webond.activity.model;

import org.springframework.data.jpa.repository.JpaRepository;

import com.webond.activity.model.ActivityTypeVO;

public interface ActivityTypeRepository extends JpaRepository<ActivityTypeVO, Integer> {
}