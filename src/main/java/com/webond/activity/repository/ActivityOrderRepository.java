package com.webond.activity.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.webond.activity.model.ActivityOrderVO;

public interface ActivityOrderRepository extends JpaRepository<ActivityOrderVO, Integer> {

	List<ActivityOrderVO> findByActivityId(Integer activityId);

	List<ActivityOrderVO> findByBuyerMemberId(Integer buyerMemberId);

	List<ActivityOrderVO> findByActivityIdAndOrderStatus(Integer activityId, Byte orderStatus);

	List<ActivityOrderVO> findByOrderStatus(Byte orderStatus);

	List<ActivityOrderVO> findByOrderStatusAndApprovedAtBefore(Byte orderStatus, LocalDateTime approvedAt);

	Long countByActivityIdAndOrderStatus(Integer activityId, Byte orderStatus);

	boolean existsByActivityIdAndBuyerMemberIdAndOrderStatus(Integer activityId, Integer buyerMemberId, Byte orderStatus);

	boolean existsByActivityIdAndBuyerMemberIdAndOrderStatusIn(Integer activityId, Integer buyerMemberId,
			List<Byte> orderStatuses);

	@Query("select coalesce(sum(o.bookingCount), 0) from ActivityOrderVO o where o.activityId = :activityId and o.orderStatus = :orderStatus")
	Integer sumBookingCountByActivityIdAndOrderStatus(@Param("activityId") Integer activityId,
			@Param("orderStatus") Byte orderStatus);

}
