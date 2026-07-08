package com.webond.activity.model;

import java.io.Serializable;
import java.time.LocalDateTime;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "ACT_ORDER")
public class ActivityOrderVO implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ACTIVITY_ORDER_ID")
	private Integer activityOrderId;

	// 活動ID
	@NotNull(message = "請選擇活動")
	@Column(name = "ACTIVITY_ID")
	private Integer activityId;

	// 買家會員ID
	@NotNull(message = "請輸入會員ID")
	@Min(value = 1, message = "會員ID不可小於1")
	@Column(name = "BUYER_MEMBER_ID")
	private Integer buyerMemberId;

	// 員工ID
	@Column(name = "EMPLOYEE_ID")
	private Integer employeeId;

	// 訂單狀態
	@Column(name = "ORDER_STATUS")
	@NotNull(message = "請選擇訂單狀態")
	private Byte orderStatus = 0;

	// 報名人數
	@NotNull(message = "請輸入報名人數")
	@Min(value = 1, message = "報名人數至少1人")
	@Column(name = "BOOKING_COUNT")
	private Integer bookingCount = 1;

	// 活動單價
	@NotNull(message = "請輸入活動價格")
	@Min(value = 0, message = "活動價格不可小於0")
	@Column(name = "ACTIVITY_PRICE")
	private Integer activityPrice;

	// 訂單總金額
	@NotNull(message = "請輸入訂單總金額")
	@Min(value = 0, message = "總金額不可小於0")
	@Column(name = "TOTAL_AMOUNT")
	private Integer totalAmount;

	// 備註
	@Size(max = 500)
	@Column(name = "ORDER_NOTE")
	private String orderNote;

	// 付款方式
	@Column(name = "ACTIVITY_PAYMENT_METHOD")
	@NotNull(message = "請選擇付款方式")
	private Byte activityPaymentMethod;

	// 付款時間
	@Column(name = "PAID_AT")
	private LocalDateTime paidAt;

	// 主辦審核通過時間
	@Column(name = "APPROVED_AT")
	private LocalDateTime approvedAt;

	// 完成時間
	@Column(name = "ACTIVITY_COMPLETED_AT")
	private LocalDateTime activityCompletedAt;

	// 買家評分
	@Column(name = "BUYER_RATE_SELLER")
	private Byte buyerRateSeller;

	// 買家評論
	@Size(max = 500)
	@Column(name = "BUYER_REVIEW_COMMENT")
	private String buyerReviewComment;

	// 買家評論時間
	@Column(name = "BUYER_REVIEWED_AT")
	private LocalDateTime buyerReviewedAt;

	// 賣家評分
	@Column(name = "SELLER_RATE_BUYER")
	private Byte sellerRateBuyer;

	// 賣家評論
	@Size(max = 500)
	@Column(name = "SELLER_REVIEW_COMMENT")
	private String sellerReviewComment;

	// 賣家評論時間
	@Column(name = "SELLER_REVIEWED_AT")
	private LocalDateTime sellerReviewedAt;

	// 撥款狀態
	@Column(name = "PAYOUT_AMOUNT")
	private Boolean payoutAmount = false;

	// 退款原因
	@Size(max = 255)
	@Column(name = "REFUND_REASON")
	private String refundReason;

	// 退款狀態
	@Column(name = "REFUND_STATUS")
	private Byte refundStatus;

	// ================= Getter & Setter =================

	public Integer getActivityOrderId() {
		return activityOrderId;
	}

	public void setActivityOrderId(Integer activityOrderId) {
		this.activityOrderId = activityOrderId;
	}

	public Integer getActivityId() {
		return activityId;
	}

	public void setActivityId(Integer activityId) {
		this.activityId = activityId;
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

	public Byte getOrderStatus() {
		return orderStatus;
	}

	public void setOrderStatus(Byte orderStatus) {
		this.orderStatus = orderStatus;
	}

	public Integer getBookingCount() {
		return bookingCount;
	}

	public void setBookingCount(Integer bookingCount) {
		this.bookingCount = bookingCount;
	}

	public Integer getActivityPrice() {
		return activityPrice;
	}

	public void setActivityPrice(Integer activityPrice) {
		this.activityPrice = activityPrice;
	}

	public Integer getTotalAmount() {
		return totalAmount;
	}

	public void setTotalAmount(Integer totalAmount) {
		this.totalAmount = totalAmount;
	}

	public String getOrderNote() {
		return orderNote;
	}

	public void setOrderNote(String orderNote) {
		this.orderNote = orderNote;
	}

	public Byte getActivityPaymentMethod() {
		return activityPaymentMethod;
	}

	public void setActivityPaymentMethod(Byte activityPaymentMethod) {
		this.activityPaymentMethod = activityPaymentMethod;
	}

	public LocalDateTime getPaidAt() {
		return paidAt;
	}

	public void setPaidAt(LocalDateTime paidAt) {
		this.paidAt = paidAt;
	}

	public LocalDateTime getApprovedAt() {
		return approvedAt;
	}

	public void setApprovedAt(LocalDateTime approvedAt) {
		this.approvedAt = approvedAt;
	}

	public LocalDateTime getActivityCompletedAt() {
		return activityCompletedAt;
	}

	public void setActivityCompletedAt(LocalDateTime activityCompletedAt) {
		this.activityCompletedAt = activityCompletedAt;
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

	public Boolean getPayoutAmount() {
		return payoutAmount;
	}

	public void setPayoutAmount(Boolean payoutAmount) {
		this.payoutAmount = payoutAmount;
	}

	public String getRefundReason() {
		return refundReason;
	}

	public void setRefundReason(String refundReason) {
		this.refundReason = refundReason;
	}

	public Byte getRefundStatus() {
		return refundStatus;
	}

	public void setRefundStatus(Byte refundStatus) {
		this.refundStatus = refundStatus;
	}
}
