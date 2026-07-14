package com.webond.orderManagement.dto;

import java.time.LocalDateTime;

public class RefundPayoutDTO {

	private String orderType; // ONE_ON_ONE / GROUP / VENUE
	private String orderTypeName; // 一對一服務 / 揪團活動 / 場地租借

	private Integer orderId;
	private Integer memberId;

	private Integer totalAmount;

	private Byte refundStatus;
	private Integer refundAmount;
	private String refundReason;

	private Boolean payoutDone;

	private Integer employeeId;
	private String employeeName;

	private LocalDateTime handledAt;
	private LocalDateTime createdAt;

	public String getOrderType() {
		return orderType;
	}

	public void setOrderType(String orderType) {
		this.orderType = orderType;
	}

	public String getOrderTypeName() {
		return orderTypeName;
	}

	public void setOrderTypeName(String orderTypeName) {
		this.orderTypeName = orderTypeName;
	}

	public Integer getOrderId() {
		return orderId;
	}

	public void setOrderId(Integer orderId) {
		this.orderId = orderId;
	}

	public Integer getMemberId() {
		return memberId;
	}

	public void setMemberId(Integer memberId) {
		this.memberId = memberId;
	}

	public Integer getTotalAmount() {
		return totalAmount;
	}

	public void setTotalAmount(Integer totalAmount) {
		this.totalAmount = totalAmount;
	}

	public Byte getRefundStatus() {
		return refundStatus;
	}

	public void setRefundStatus(Byte refundStatus) {
		this.refundStatus = refundStatus;
	}

	public Integer getRefundAmount() {
		return refundAmount;
	}

	public void setRefundAmount(Integer refundAmount) {
		this.refundAmount = refundAmount;
	}

	public String getRefundReason() {
		return refundReason;
	}

	public void setRefundReason(String refundReason) {
		this.refundReason = refundReason;
	}

	public Boolean getPayoutDone() {
		return payoutDone;
	}

	public void setPayoutDone(Boolean payoutDone) {
		this.payoutDone = payoutDone;
	}

	public Integer getEmployeeId() {
		return employeeId;
	}

	public void setEmployeeId(Integer employeeId) {
		this.employeeId = employeeId;
	}

	public String getEmployeeName() {
		return employeeName;
	}

	public void setEmployeeName(String employeeName) {
		this.employeeName = employeeName;
	}

	public LocalDateTime getHandledAt() {
		return handledAt;
	}

	public void setHandledAt(LocalDateTime handledAt) {
		this.handledAt = handledAt;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}
}
