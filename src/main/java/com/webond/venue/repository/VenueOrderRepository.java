package com.webond.venue.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.webond.venue.model.VenueOrderVO;

public interface VenueOrderRepository
		extends JpaRepository<VenueOrderVO, Integer>, JpaSpecificationExecutor<VenueOrderVO> {

	List<VenueOrderVO> findByMember_MemberId(Integer memberId);
	
	List<VenueOrderVO> findByOrderStatusAndCreatedAtBefore(Byte orderStatus, LocalDateTime dateTime);
}
