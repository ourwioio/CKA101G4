package com.webond.activity.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.webond.activity.model.ActRptVO;

@Repository
public interface ActRptRepository extends JpaRepository<ActRptVO, Integer>{
	
	// 找出特定狀態
	Page<ActRptVO> findByActRptStatus(Integer status, Pageable pageable);
	
}
