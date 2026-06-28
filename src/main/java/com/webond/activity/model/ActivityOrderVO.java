package com.webond.activity.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "ACTIVITY_ORDER")
public class ActivityOrderVO {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ACTIVITY_ORDER_ID")
	private Integer activityOrderId;

	@Column(name = "ACTIVITY_ID", nullable = false)
	private Integer activityId;

	@Column(name = "MEMBER_ID", nullable = false)
	private Integer memberId;

	@Column(name = "ORDER_TOTAL", nullable = false)
	private Integer orderTotal = 0;

	@Column(name = "PAYMENT_STATUS", nullable = false)
	private Integer paymentStatus = 0;

	@Column(name = "CREATED_AT", updatable = false)
	private LocalDateTime createdAt;

	@PrePersist
	protected void onCreate() {
		this.createdAt = LocalDateTime.now();
	}

	// Getters and Setters
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

	public Integer getMemberId() {
		return memberId;
	}

	public void setMemberId(Integer memberId) {
		this.memberId = memberId;
	}

	public Integer getOrderTotal() {
		return orderTotal;
	}

	public void setOrderTotal(Integer orderTotal) {
		this.orderTotal = orderTotal;
	}

	public Integer getPaymentStatus() {
		return paymentStatus;
	}

	public void setPaymentStatus(Integer paymentStatus) {
		this.paymentStatus = paymentStatus;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}
}