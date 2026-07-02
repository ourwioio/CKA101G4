package com.webond.service.model;

import java.time.LocalDateTime;

import jakarta.persistence.*;

@Entity
@Table(name = "SERVICE_ORDER")
public class ServiceOrderVO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "SERVICE_ORDER_ID")
    private Integer serviceOrderId;

    @Column(name = "SERVICE_SLOT_ID")
    private Integer serviceSlotId;

    @Column(name = "SERVICE_ID")
    private Integer serviceId;

    @Column(name = "BUYER_MEMBER_ID")
    private Integer buyerMemberId;

    @Column(name = "EMPLOYEE_ID")
    private Integer employeeId;

    @Column(name = "ORDER_HOURLY_RATE")
    private Integer orderHourlyRate;

    @Column(name = "TOTAL_AMOUNT")
    private Integer totalAmount;

    @Column(name = "ORDER_STATUS")
    private Byte orderStatus;

    @Column(name = "BUYER_REQUEST_NOTE")
    private String buyerRequestNote;

    @Column(name = "SELLER_REQUIREMENT_NOTE")
    private String sellerRequirementNote;

    @Column(name = "SERVICE_PAYMENT_METHOD")
    private Byte servicePaymentMethod;

    @Column(name = "CREATED_AT")
    private LocalDateTime createdAt;

    @Column(name = "SERVICE_COMPLETED_AT")
    private LocalDateTime serviceCompletedAt;

    @Column(name = "BUYER_RATE_SELLER")
    private Byte buyerRateSeller;

    @Column(name = "BUYER_REVIEW_COMMENT")
    private String buyerReviewComment;

    @Column(name = "BUYER_REVIEWED_AT")
    private LocalDateTime buyerReviewedAt;

    @Column(name = "SELLER_RATE_BUYER")
    private Byte sellerRateBuyer;

    @Column(name = "SELLER_REVIEW_COMMENT")
    private String sellerReviewComment;

    @Column(name = "SELLER_REVIEWED_AT")
    private LocalDateTime sellerReviewedAt;

    @Column(name = "PAYOUT_STATUS")
    private Byte payoutStatus;

    @Column(name = "REFUND_STATUS")
    private Byte refundStatus;

    @Column(name = "HANDLED_AT")
    private LocalDateTime handledAt;

    @Column(name = "CANCELLED_BY_ROLE")
    private Byte cancelledByRole;

    @Column(name = "CANCEL_REASON")
    private String cancelReason;

    @Column(name = "CANCELLED_AT")
    private LocalDateTime cancelledAt;
    
    @Column(name = "REFUND_AMOUNT")
    private Integer refundAmount;

    @Column(name = "SELLER_CONFIRM_EXPIRES_AT")
    private LocalDateTime sellerConfirmExpiresAt;
    
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

	@Column(name = "PAYMENT_EXPIRES_AT")
    private LocalDateTime paymentExpiresAt;
    public Integer getRefundAmount() {
		return refundAmount;
	}

	public void setRefundAmount(Integer refundAmount) {
		this.refundAmount = refundAmount;
	}

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
}