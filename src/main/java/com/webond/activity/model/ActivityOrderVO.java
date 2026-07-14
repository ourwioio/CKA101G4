package com.webond.activity.model;

import java.io.Serializable;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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

	@NotNull(message = "\u8acb\u63d0\u4f9b\u6d3b\u52d5\u7de8\u865f")
	@Column(name = "ACTIVITY_ID")
	private Integer activityId;

	@NotNull(message = "\u8acb\u63d0\u4f9b\u5831\u540d\u6703\u54e1\u7de8\u865f")
	@Min(value = 1, message = "\u5831\u540d\u6703\u54e1\u7de8\u865f\u4e0d\u53ef\u5c0f\u65bc 1")
	@Column(name = "BUYER_MEMBER_ID")
	private Integer buyerMemberId;

	@Column(name = "EMPLOYEE_ID")
	private Integer employeeId;

	@NotNull(message = "\u8acb\u9078\u64c7\u8a02\u55ae\u72c0\u614b")
	@Column(name = "ORDER_STATUS")
	private Byte orderStatus = 2;

	@NotNull(message = "\u8acb\u8f38\u5165\u5831\u540d\u4eba\u6578")
	@Min(value = 1, message = "\u5831\u540d\u4eba\u6578\u81f3\u5c11\u70ba 1")
	@Column(name = "BOOKING_COUNT")
	private Integer bookingCount = 1;

	@NotNull(message = "\u8acb\u8f38\u5165\u6d3b\u52d5\u50f9\u683c")
	@Min(value = 0, message = "\u6d3b\u52d5\u50f9\u683c\u4e0d\u53ef\u5c0f\u65bc 0")
	@Column(name = "ACTIVITY_PRICE")
	private Integer activityPrice;

	@NotNull(message = "\u8acb\u8f38\u5165\u8a02\u55ae\u91d1\u984d")
	@Min(value = 0, message = "\u8a02\u55ae\u91d1\u984d\u4e0d\u53ef\u5c0f\u65bc 0")
	@Column(name = "TOTAL_AMOUNT")
	private Integer totalAmount;

	@Size(max = 500, message = "\u8a02\u55ae\u5099\u8a3b\u6700\u591a 500 \u5b57")
	@Column(name = "ORDER_NOTE")
	private String orderNote;

	@Column(name = "ACTIVITY_PAYMENT_METHOD")
	private Byte activityPaymentMethod;

	@Column(name = "PAID_AT")
	private LocalDateTime paidAt;

	@Column(name = "APPROVED_AT")
	private LocalDateTime approvedAt;

	@Column(name = "ACTIVITY_COMPLETED_AT")
	private LocalDateTime activityCompletedAt;

	@Column(name = "BUYER_RATE_SELLER")
	private Byte buyerRateSeller;

	@Size(max = 500, message = "\u8cb7\u5bb6\u8a55\u8ad6\u6700\u591a 500 \u5b57")
	@Column(name = "BUYER_REVIEW_COMMENT")
	private String buyerReviewComment;

	@Column(name = "BUYER_REVIEWED_AT")
	private LocalDateTime buyerReviewedAt;

	@Column(name = "SELLER_RATE_BUYER")
	private Byte sellerRateBuyer;

	@Size(max = 500, message = "\u8ce3\u5bb6\u8a55\u8ad6\u6700\u591a 500 \u5b57")
	@Column(name = "SELLER_REVIEW_COMMENT")
	private String sellerReviewComment;

	@Column(name = "SELLER_REVIEWED_AT")
	private LocalDateTime sellerReviewedAt;

	@Column(name = "PAYOUT_AMOUNT")
	private Boolean payoutAmount = false;

	@Size(max = 255, message = "\u9000\u6b3e\u539f\u56e0\u6700\u591a 255 \u5b57")
	@Column(name = "REFUND_REASON")
	private String refundReason;

	@Column(name = "REFUND_STATUS")
	private Byte refundStatus = 0;

	public Integer getActivityOrderId() { return activityOrderId; }
	public void setActivityOrderId(Integer activityOrderId) { this.activityOrderId = activityOrderId; }
	public Integer getActivityId() { return activityId; }
	public void setActivityId(Integer activityId) { this.activityId = activityId; }
	public Integer getBuyerMemberId() { return buyerMemberId; }
	public void setBuyerMemberId(Integer buyerMemberId) { this.buyerMemberId = buyerMemberId; }
	public Integer getEmployeeId() { return employeeId; }
	public void setEmployeeId(Integer employeeId) { this.employeeId = employeeId; }
	public Byte getOrderStatus() { return orderStatus; }
	public void setOrderStatus(Byte orderStatus) { this.orderStatus = orderStatus; }
	public Integer getBookingCount() { return bookingCount; }
	public void setBookingCount(Integer bookingCount) { this.bookingCount = bookingCount; }
	public Integer getActivityPrice() { return activityPrice; }
	public void setActivityPrice(Integer activityPrice) { this.activityPrice = activityPrice; }
	public Integer getTotalAmount() { return totalAmount; }
	public void setTotalAmount(Integer totalAmount) { this.totalAmount = totalAmount; }
	public String getOrderNote() { return orderNote; }
	public void setOrderNote(String orderNote) { this.orderNote = orderNote; }
	public Byte getActivityPaymentMethod() { return activityPaymentMethod; }
	public void setActivityPaymentMethod(Byte activityPaymentMethod) { this.activityPaymentMethod = activityPaymentMethod; }
	public LocalDateTime getPaidAt() { return paidAt; }
	public void setPaidAt(LocalDateTime paidAt) { this.paidAt = paidAt; }
	public LocalDateTime getApprovedAt() { return approvedAt; }
	public void setApprovedAt(LocalDateTime approvedAt) { this.approvedAt = approvedAt; }
	public LocalDateTime getActivityCompletedAt() { return activityCompletedAt; }
	public void setActivityCompletedAt(LocalDateTime activityCompletedAt) { this.activityCompletedAt = activityCompletedAt; }
	public Byte getBuyerRateSeller() { return buyerRateSeller; }
	public void setBuyerRateSeller(Byte buyerRateSeller) { this.buyerRateSeller = buyerRateSeller; }
	public String getBuyerReviewComment() { return buyerReviewComment; }
	public void setBuyerReviewComment(String buyerReviewComment) { this.buyerReviewComment = buyerReviewComment; }
	public LocalDateTime getBuyerReviewedAt() { return buyerReviewedAt; }
	public void setBuyerReviewedAt(LocalDateTime buyerReviewedAt) { this.buyerReviewedAt = buyerReviewedAt; }
	public Byte getSellerRateBuyer() { return sellerRateBuyer; }
	public void setSellerRateBuyer(Byte sellerRateBuyer) { this.sellerRateBuyer = sellerRateBuyer; }
	public String getSellerReviewComment() { return sellerReviewComment; }
	public void setSellerReviewComment(String sellerReviewComment) { this.sellerReviewComment = sellerReviewComment; }
	public LocalDateTime getSellerReviewedAt() { return sellerReviewedAt; }
	public void setSellerReviewedAt(LocalDateTime sellerReviewedAt) { this.sellerReviewedAt = sellerReviewedAt; }
	public Boolean getPayoutAmount() { return payoutAmount; }
	public void setPayoutAmount(Boolean payoutAmount) { this.payoutAmount = payoutAmount; }
	public String getRefundReason() { return refundReason; }
	public void setRefundReason(String refundReason) { this.refundReason = refundReason; }
	public Byte getRefundStatus() { return refundStatus; }
	public void setRefundStatus(Byte refundStatus) { this.refundStatus = refundStatus; }
}
