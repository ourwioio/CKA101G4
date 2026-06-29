package com.webond.activity.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.webond.activity.model.ActivityTypeVO;

public interface ActivityTypeRepository extends JpaRepository<ActivityTypeVO, Integer> {
}