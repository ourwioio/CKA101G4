package com.webond.service.repository;

import java.time.LocalDateTime;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.webond.service.model.ServiceOrderVO;

public interface ServiceReviewRepository extends JpaRepository<ServiceOrderVO, Integer>{
	@Modifying
	@Query("UPDATE ServiceOrderVO o SET o.buyerRateSeller = :rate, o.buyerReviewComment = :comment, o.buyerReviewedAt = :reviewAt WHERE o.serviceOrderId = :orderId")
	int submitBuyerReview(@Param("orderId") Integer orderId,
						  @Param("rate") Byte rate,
						  @Param("comment") String comment,
						  @Param("reviewAt") LocalDateTime reviewAt);
	
	@Modifying
	@Query("UPDATE ServiceOrderVO o SET o.sellerRateBuyer = :rate, o.sellerReviewComment = :comment, o.sellerReviewedAt = :reviewAt WHERE o.serviceOrderId = :orderId")
	int submitsellerReview(@Param("orderId") Integer orderId,
						  @Param("rate") Byte rate,
						  @Param("comment") String comment,
						  @Param("reviewAt") LocalDateTime reviewAt);
}
