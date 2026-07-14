package com.webond.venue.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.webond.venue.model.VenueOrderVO;

public interface VenueOrderRepository
		extends JpaRepository<VenueOrderVO, Integer>, JpaSpecificationExecutor<VenueOrderVO> {

	List<VenueOrderVO> findByMember_MemberId(Integer memberId);

	List<VenueOrderVO> findByOrderStatusAndCreatedAtBefore(Byte orderStatus, LocalDateTime dateTime);

	@Query("""
			SELECT o FROM VenueOrderVO o
			JOIN o.venueVO v
			JOIN v.member m
			WHERE m.memberId = :ownerMemberId AND o.orderStatus = :orderStatus
			""")
	List<VenueOrderVO> findPaidOrdersByVenueOwner(@Param("ownerMemberId") Integer ownerMemberId,
			@Param("orderStatus") Byte orderStatus);
	
	 List<VenueOrderVO> findByOrderStatus(Byte orderStatus);
	 
	 List<VenueOrderVO> findByRefundStatus(Byte refundStatus);
	 
	 List<VenueOrderVO> findByPayoutAmount(Byte payoutAmount);
	 
	 List<VenueOrderVO> findByVenueVO_VenueIdAndVenueRatingIsNotNullOrderByHandledAtDesc(Integer venueId);
}
