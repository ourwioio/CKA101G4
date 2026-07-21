package com.webond.service.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.webond.service.model.ServiceOrderVO;

public interface ServiceOrderRepository
        extends JpaRepository<ServiceOrderVO, Integer> {
	
	//查詢給賣家的評價
	List<ServiceOrderVO> findByServiceIdAndBuyerRateSellerIsNotNullOrderByBuyerReviewedAtDesc(Integer serviceId);

    // =========================================================
    // 前台：買家／賣家訂單
    // =========================================================

    // 查某個買家的所有訂單
    List<ServiceOrderVO> findByBuyerMemberId(
            Integer buyerMemberId
    );

    // 查某個賣家的所有訂單
    List<ServiceOrderVO> findBySellerMemberId(
            Integer sellerMemberId
    );

    // =========================================================
    // 服務與時段查詢
    // =========================================================

    // 查某個服務的所有訂單
    List<ServiceOrderVO> findByServiceId(
            Integer serviceId
    );

    /*
     * 同一個時段可以同時有多位買家提出申請，
     * 所以這裡必須回傳 List，不能只回傳一筆。
     */
    List<ServiceOrderVO> findAllByServiceSlotId(
            Integer serviceSlotId
    );

    // 判斷某個服務是否已有訂單紀錄
    boolean existsByServiceId(
            Integer serviceId
    );

    // =========================================================
    // 一般狀態查詢
    // =========================================================

    // 依訂單狀態查詢
    List<ServiceOrderVO> findByOrderStatus(
            Byte orderStatus
    );

    // 依付款方式查詢
    List<ServiceOrderVO> findByServicePaymentMethod(
            Byte servicePaymentMethod
    );

    // 依退款狀態查詢
    List<ServiceOrderVO> findByRefundStatus(
            Byte refundStatus
    );

    // 依撥款狀態查詢
    List<ServiceOrderVO> findByPayoutStatus(
            Byte payoutStatus
    );

    // =========================================================
    // 後台：撥款查詢
    // =========================================================

    /*
     * 例如：
     * ORDER_STATUS = 3 已完成
     * PAYOUT_STATUS = 0 未撥款
     *
     * 或：
     * ORDER_STATUS = 3 已完成
     * PAYOUT_STATUS = 1 已撥款
     */
    List<ServiceOrderVO> findByOrderStatusAndPayoutStatus(
            Byte orderStatus,
            Byte payoutStatus
    );

    // =========================================================
    // 後台：退款查詢
    // =========================================================

    /*
     * 例如：
     * ORDER_STATUS = 4 已取消
     * REFUND_STATUS = 0 不需要退款
     *
     * ORDER_STATUS = 4 已取消
     * REFUND_STATUS = 1 待退款
     *
     * ORDER_STATUS = 4 已取消
     * REFUND_STATUS = 2 已退款
     */
    List<ServiceOrderVO> findByOrderStatusAndRefundStatus(
            Byte orderStatus,
            Byte refundStatus
    );

    // =========================================================
    // 買家付款成功後，查詢其他相同時段申請
    // =========================================================

    /*
     * 查詢：
     * 1. 相同 SERVICE_SLOT_ID
     * 2. 訂單狀態仍是待賣家確認
     * 3. 排除已經付款成功的那一筆訂單
     *
     * 查出的其他訂單會在買家付款成功後，
     * 由系統取消並發送通知。
     */
    List<ServiceOrderVO>
            findByServiceSlotIdAndOrderStatusAndServiceOrderIdNot(
                    Integer serviceSlotId,
                    Byte orderStatus,
                    Integer serviceOrderId
            );

    // =========================================================
    // 排程：逾期訂單查詢
    // =========================================================

    /*
     * 查詢賣家確認期限已到，
     * 且狀態仍為待賣家確認的訂單。
     */
    List<ServiceOrderVO>
            findByOrderStatusAndSellerConfirmExpiresAtLessThanEqual(
                    Byte orderStatus,
                    LocalDateTime now
            );

    /*
     * 查詢買家付款期限已到，
     * 且狀態仍為待買家付款的訂單。
     */
    List<ServiceOrderVO>
            findByOrderStatusAndPaymentExpiresAtLessThanEqual(
                    Byte orderStatus,
                    LocalDateTime now
            );

    // =========================================================
    // 後台：訂單金額統計
    // =========================================================

    // 已完成訂單總金額
    @Query("""
           select coalesce(sum(o.totalAmount), 0)
           from ServiceOrderVO o
           where o.orderStatus = 3
           """)
    Long sumCompletedOrderTotalAmount();

    // 已完成但尚未撥款的訂單總金額
    @Query("""
           select coalesce(sum(o.totalAmount), 0)
           from ServiceOrderVO o
           where o.orderStatus = 3
             and o.payoutStatus = 0
           """)
    Long sumCompletedUnpaidTotalAmount();

    // 已完成但尚未撥款的訂單筆數
    @Query("""
           select count(o)
           from ServiceOrderVO o
           where o.orderStatus = 3
             and o.payoutStatus = 0
           """)
    Long countCompletedUnpaidOrders();

    // =========================================================
    // 後台：退款統計
    // =========================================================

    // 已取消且待退款的訂單總退款金額
    @Query("""
           select coalesce(sum(o.refundAmount), 0)
           from ServiceOrderVO o
           where o.orderStatus = 4
             and o.refundStatus = 1
           """)
    Long sumPendingRefundAmount();

    // 已取消且待退款的訂單筆數
    @Query("""
           select count(o)
           from ServiceOrderVO o
           where o.orderStatus = 4
             and o.refundStatus = 1
           """)
    Long countPendingRefundOrders();

    // =========================================================
    // 評價查詢
    // =========================================================

    List<ServiceOrderVO>
            findBySellerMemberIdAndBuyerReviewCommentIsNotNull(
                    Integer sellerMemberId
            );

    List<ServiceOrderVO>
            findByBuyerMemberIdAndSellerReviewCommentIsNotNull(
                    Integer buyerMemberId
            );
    
 // 已完成訂單筆數
    @Query("""
           select count(o)
           from ServiceOrderVO o
           where o.orderStatus = 3
           """)
    Long countCompletedOrders();
    
    /*
     * 查詢服務時間已結束，
     * 且訂單狀態仍為已成立的訂單。
     *
     * ORDER_STATUS = 2
     * SLOT_END_TIME_SNAPSHOT <= 現在
     */
    List<ServiceOrderVO>
            findByOrderStatusAndSlotEndTimeSnapshotLessThanEqual(
                    Byte orderStatus,
                    LocalDateTime now
            );
}