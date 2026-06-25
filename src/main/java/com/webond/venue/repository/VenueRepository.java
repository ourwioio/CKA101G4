package com.webond.venue.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.webond.venue.model.VenueVO;

public interface VenueRepository extends JpaRepository<VenueVO, Integer> {
	
	List<VenueVO> findByMember_MemberId(Integer memberId);
}
