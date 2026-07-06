package com.webond.service.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

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
    
    //查賣方訂單
    List<ServiceOrderVO> findBySellerMemberId(Integer sellerMemberId);
    
 // 依訂單狀態 + 撥款狀態查詢
    List<ServiceOrderVO> findByOrderStatusAndPayoutStatus(Byte orderStatus, Byte payoutStatus);

    // 已完成訂單總金額
    @Query("""
           select coalesce(sum(o.totalAmount), 0)
           from ServiceOrderVO o
           where o.orderStatus = 3
           """)
    Integer sumCompletedOrderTotalAmount();

    // 已完成但未撥款的訂單總金額
    @Query("""
           select coalesce(sum(o.totalAmount), 0)
           from ServiceOrderVO o
           where o.orderStatus = 3
           and o.payoutStatus = 0
           """)
    Integer sumCompletedUnpaidTotalAmount();

    // 已完成但未撥款的訂單筆數
    @Query("""
           select count(o)
           from ServiceOrderVO o
           where o.orderStatus = 3
           and o.payoutStatus = 0
           """)
    Long countCompletedUnpaidOrders();
}