package com.webond.venue.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.webond.venue.model.VenueOrderVO;
import com.webond.venue.model.VenueVO;

public interface VenueOrderRepository extends JpaRepository<VenueOrderVO, Integer>{
	
	List<VenueOrderVO> findByMember_MemberId(Integer memberId);
}
