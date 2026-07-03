package com.webond.activity.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.webond.activity.model.ActivityVO;

public interface ActivityRepository extends JpaRepository<ActivityVO, Integer> {

	List<ActivityVO> findByMemberId(Integer memberId);

}