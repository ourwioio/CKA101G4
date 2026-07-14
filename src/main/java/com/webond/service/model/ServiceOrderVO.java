package com.webond.service.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "SERVICE_ORDER")
public class ServiceOrderVO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "SERVICE_ORDER_ID")
    private Integer serviceOrderId;

    @Column(name = "SERVICE_SLOT_ID", nullable = false)
    private Integer serviceSlotId;

    @Column(name = "SERVICE_ID", nullable = false)
    private Integer serviceId;

    @Column(name = "BUYER_MEMBER_ID", nullable = false)
    private Integer buyerMemberId;

    @Column(name = "EMPLOYEE_ID")
    private Integer employeeId;

    @Column(name = "ORDER_HOURLY_RATE", nullable = false)
    private Integer orderHourlyRate;

    @Column(name = "TOTAL_AMOUNT", nullable = false)
    private Integer totalAmount;

    // 0：待賣家確認
    // 1：待買家付款
    // 2：已成立
    // 3：已完成
    // 4：已取消
    @Column(name = "ORDER_STATUS", nullable = false)
    private Byte orderStatus = 0;

    @Column(name = "BUYER_REQUEST_NOTE", length = 500)
    private String buyerRequestNote;

    @Column(name = "SELLER_REQUIREMENT_NOTE", length = 500)
    private String sellerRequirementNote;

    // 0：信用卡
    // 1：ATM轉帳
    @Column(name = "SERVICE_PAYMENT_METHOD")
    private Byte servicePaymentMethod;

    @Column(name = "CREATED_AT", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "SERVICE_COMPLETED_AT")
    private LocalDateTime serviceCompletedAt;

    @Column(name = "BUYER_RATE_SELLER")
    private Byte buyerRateSeller;

    @Column(name = "BUYER_REVIEW_COMMENT", length = 500)
    private String buyerReviewComment;

    @Column(name = "BUYER_REVIEWED_AT")
    private LocalDateTime buyerReviewedAt;

    @Column(name = "SELLER_RATE_BUYER")
    private Byte sellerRateBuyer;

    @Column(name = "SELLER_REVIEW_COMMENT", length = 500)
    private String sellerReviewComment;

    @Column(name = "SELLER_REVIEWED_AT")
    private LocalDateTime sellerReviewedAt;

    // 0：未撥款
    // 1：已撥款
    @Column(name = "PAYOUT_STATUS", nullable = false)
    private Byte payoutStatus = 0;

    // 0：無退款
    // 1：待退款
    // 2：已退款
    @Column(name = "REFUND_STATUS", nullable = false)
    private Byte refundStatus = 0;

    @Column(name = "HANDLED_AT")
    private LocalDateTime handledAt;

    // 0：買方取消
    // 1：賣方取消
    // 2：後台取消
    // 3：系統逾時取消
    @Column(name = "CANCELLED_BY_ROLE")
    private Byte cancelledByRole;

    @Column(name = "CANCEL_REASON", length = 500)
    private String cancelReason;

    @Column(name = "CANCELLED_AT")
    private LocalDateTime cancelledAt;

    @Column(name = "REFUND_AMOUNT")
    private Integer refundAmount = 0;

    @Column(name = "SELLER_CONFIRM_EXPIRES_AT")
    private LocalDateTime sellerConfirmExpiresAt;

    @Column(name = "PAYMENT_EXPIRES_AT")
    private LocalDateTime paymentExpiresAt;

    // =========================================================
    // 訂單快照欄位
    // 建立訂單當下寫入，避免服務後續修改影響歷史訂單
    // =========================================================

    @Column(name = "SELLER_MEMBER_ID", nullable = false)
    private Integer sellerMemberId;

    @Column(name = "SERVICE_NAME_SNAPSHOT", nullable = false, length = 100)
    private String serviceNameSnapshot;

    @Column(name = "SERVICE_TYPE_NAME_SNAPSHOT", nullable = false, length = 500)
    private String serviceTypeNameSnapshot;

    // 資料庫已改為可空
    @Column(name = "SERVICE_DESCRIPTION_SNAPSHOT", length = 500)
    private String serviceDescriptionSnapshot;

    @Column(name = "SLOT_START_TIME_SNAPSHOT", nullable = false)
    private LocalDateTime slotStartTimeSnapshot;

    @Column(name = "SLOT_END_TIME_SNAPSHOT", nullable = false)
    private LocalDateTime slotEndTimeSnapshot;

    @Column(name = "SERVICE_CITY_SNAPSHOT", nullable = false, length = 20)
    private String serviceCitySnapshot;

    @Column(name = "SERVICE_DISTRICT_SNAPSHOT", nullable = false, length = 20)
    private String serviceDistrictSnapshot;

    @Column(name = "SERVICE_LOCATION_SNAPSHOT", nullable = false, length = 255)
    private String serviceLocationSnapshot;

    // =========================================================
    // Getter / Setter
    // =========================================================

    public Integer getServiceOrderId() {
        return serviceOrderId;
    }

    public void setServiceOrderId(Integer serviceOrderId) {
        this.serviceOrderId = serviceOrderId;
    }

    public Integer getServiceSlotId() {
        return serviceSlotId;
    }

    public void setServiceSlotId(Integer serviceSlotId) {
        this.serviceSlotId = serviceSlotId;
    }

    public Integer getServiceId() {
        return serviceId;
    }

    public void setServiceId(Integer serviceId) {
        this.serviceId = serviceId;
    }

    public Integer getBuyerMemberId() {
        return buyerMemberId;
    }

    public void setBuyerMemberId(Integer buyerMemberId) {
        this.buyerMemberId = buyerMemberId;
    }

    public Integer getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Integer employeeId) {
        this.employeeId = employeeId;
    }

    public Integer getOrderHourlyRate() {
        return orderHourlyRate;
    }

    public void setOrderHourlyRate(Integer orderHourlyRate) {
        this.orderHourlyRate = orderHourlyRate;
    }

    public Integer getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Integer totalAmount) {
        this.totalAmount = totalAmount;
    }

    public Byte getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(Byte orderStatus) {
        this.orderStatus = orderStatus;
    }

    public String getBuyerRequestNote() {
        return buyerRequestNote;
    }

    public void setBuyerRequestNote(String buyerRequestNote) {
        this.buyerRequestNote = buyerRequestNote;
    }

    public String getSellerRequirementNote() {
        return sellerRequirementNote;
    }

    public void setSellerRequirementNote(String sellerRequirementNote) {
        this.sellerRequirementNote = sellerRequirementNote;
    }

    public Byte getServicePaymentMethod() {
        return servicePaymentMethod;
    }

    public void setServicePaymentMethod(Byte servicePaymentMethod) {
        this.servicePaymentMethod = servicePaymentMethod;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getServiceCompletedAt() {
        return serviceCompletedAt;
    }

    public void setServiceCompletedAt(LocalDateTime serviceCompletedAt) {
        this.serviceCompletedAt = serviceCompletedAt;
    }

    public Byte getBuyerRateSeller() {
        return buyerRateSeller;
    }

    public void setBuyerRateSeller(Byte buyerRateSeller) {
        this.buyerRateSeller = buyerRateSeller;
    }

    public String getBuyerReviewComment() {
        return buyerReviewComment;
    }

    public void setBuyerReviewComment(String buyerReviewComment) {
        this.buyerReviewComment = buyerReviewComment;
    }

    public LocalDateTime getBuyerReviewedAt() {
        return buyerReviewedAt;
    }

    public void setBuyerReviewedAt(LocalDateTime buyerReviewedAt) {
        this.buyerReviewedAt = buyerReviewedAt;
    }

    public Byte getSellerRateBuyer() {
        return sellerRateBuyer;
    }

    public void setSellerRateBuyer(Byte sellerRateBuyer) {
        this.sellerRateBuyer = sellerRateBuyer;
    }

    public String getSellerReviewComment() {
        return sellerReviewComment;
    }

    public void setSellerReviewComment(String sellerReviewComment) {
        this.sellerReviewComment = sellerReviewComment;
    }

    public LocalDateTime getSellerReviewedAt() {
        return sellerReviewedAt;
    }

    public void setSellerReviewedAt(LocalDateTime sellerReviewedAt) {
        this.sellerReviewedAt = sellerReviewedAt;
    }

    public Byte getPayoutStatus() {
        return payoutStatus;
    }

    public void setPayoutStatus(Byte payoutStatus) {
        this.payoutStatus = payoutStatus;
    }

    public Byte getRefundStatus() {
        return refundStatus;
    }

    public void setRefundStatus(Byte refundStatus) {
        this.refundStatus = refundStatus;
    }

    public LocalDateTime getHandledAt() {
        return handledAt;
    }

    public void setHandledAt(LocalDateTime handledAt) {
        this.handledAt = handledAt;
    }

    public Byte getCancelledByRole() {
        return cancelledByRole;
    }

    public void setCancelledByRole(Byte cancelledByRole) {
        this.cancelledByRole = cancelledByRole;
    }

    public String getCancelReason() {
        return cancelReason;
    }

    public void setCancelReason(String cancelReason) {
        this.cancelReason = cancelReason;
    }

    public LocalDateTime getCancelledAt() {
        return cancelledAt;
    }

    public void setCancelledAt(LocalDateTime cancelledAt) {
        this.cancelledAt = cancelledAt;
    }

    public Integer getRefundAmount() {
        return refundAmount;
    }

    public void setRefundAmount(Integer refundAmount) {
        this.refundAmount = refundAmount;
    }

    public LocalDateTime getSellerConfirmExpiresAt() {
        return sellerConfirmExpiresAt;
    }

    public void setSellerConfirmExpiresAt(LocalDateTime sellerConfirmExpiresAt) {
        this.sellerConfirmExpiresAt = sellerConfirmExpiresAt;
    }

    public LocalDateTime getPaymentExpiresAt() {
        return paymentExpiresAt;
    }

    public void setPaymentExpiresAt(LocalDateTime paymentExpiresAt) {
        this.paymentExpiresAt = paymentExpiresAt;
    }

    public Integer getSellerMemberId() {
        return sellerMemberId;
    }

    public void setSellerMemberId(Integer sellerMemberId) {
        this.sellerMemberId = sellerMemberId;
    }

    public String getServiceNameSnapshot() {
        return serviceNameSnapshot;
    }

    public void setServiceNameSnapshot(String serviceNameSnapshot) {
        this.serviceNameSnapshot = serviceNameSnapshot;
    }

    public String getServiceTypeNameSnapshot() {
        return serviceTypeNameSnapshot;
    }

    public void setServiceTypeNameSnapshot(String serviceTypeNameSnapshot) {
        this.serviceTypeNameSnapshot = serviceTypeNameSnapshot;
    }

    public String getServiceDescriptionSnapshot() {
        return serviceDescriptionSnapshot;
    }

    public void setServiceDescriptionSnapshot(String serviceDescriptionSnapshot) {
        this.serviceDescriptionSnapshot = serviceDescriptionSnapshot;
    }

    public LocalDateTime getSlotStartTimeSnapshot() {
        return slotStartTimeSnapshot;
    }

    public void setSlotStartTimeSnapshot(LocalDateTime slotStartTimeSnapshot) {
        this.slotStartTimeSnapshot = slotStartTimeSnapshot;
    }

    public LocalDateTime getSlotEndTimeSnapshot() {
        return slotEndTimeSnapshot;
    }

    public void setSlotEndTimeSnapshot(LocalDateTime slotEndTimeSnapshot) {
        this.slotEndTimeSnapshot = slotEndTimeSnapshot;
    }

    public String getServiceCitySnapshot() {
        return serviceCitySnapshot;
    }

    public void setServiceCitySnapshot(String serviceCitySnapshot) {
        this.serviceCitySnapshot = serviceCitySnapshot;
    }

    public String getServiceDistrictSnapshot() {
        return serviceDistrictSnapshot;
    }

    public void setServiceDistrictSnapshot(String serviceDistrictSnapshot) {
        this.serviceDistrictSnapshot = serviceDistrictSnapshot;
    }

    public String getServiceLocationSnapshot() {
        return serviceLocationSnapshot;
    }

    public void setServiceLocationSnapshot(String serviceLocationSnapshot) {
        this.serviceLocationSnapshot = serviceLocationSnapshot;
    }
}