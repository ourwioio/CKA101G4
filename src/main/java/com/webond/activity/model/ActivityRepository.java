package com.webond.activity.model;

import org.springframework.data.jpa.repository.JpaRepository;

import com.webond.activity.model.ActivityVO;

public interface ActivityRepository extends JpaRepository<ActivityVO, Integer> {
}