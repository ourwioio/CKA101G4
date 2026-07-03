package com.webond.service.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.webond.service.model.ServiceOrderVO;

public interface ServiceOrderRepository extends JpaRepository<ServiceOrderVO, Integer> {

    // 查某個買家的所有訂單
    List<ServiceOrderVO> findByBuyerMemberId(Integer buyerMemberId);

    // 查某個服務的所有訂單
    List<ServiceOrderVO> findByServiceId(Integer serviceId);

    // 查某個服務時段的訂單
    ServiceOrderVO findByServiceSlotId(Integer serviceSlotId);

    // 查訂單狀態
    List<ServiceOrderVO> findByOrderStatus(Byte orderStatus);

    // 查付款方式
    List<ServiceOrderVO> findByServicePaymentMethod(Byte servicePaymentMethod);

    // 查退款狀態
    List<ServiceOrderVO> findByRefundStatus(Byte refundStatus);

    // 查撥款狀態
    List<ServiceOrderVO> findByPayoutStatus(Byte payoutStatus);
    
    // 服務是否存在於訂單
    boolean existsByServiceId(Integer serviceId);
    
    @Query(value = """
    	    SELECT o.*
    	    FROM SERVICE_ORDER o
    	    JOIN SERVICE s
    	      ON o.SERVICE_ID = s.SERVICE_ID
    	    WHERE s.MEMBER_ID = :sellerMemberId
    	""", nativeQuery = true)
    	List<ServiceOrderVO> findBySellerMemberId(@Param("sellerMemberId") Integer sellerMemberId);
}