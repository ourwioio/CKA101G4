package com.webond.activity.model;

import java.io.Serializable;
import java.time.LocalDateTime;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "ACTIVITY_ORDER")
public class ActivityOrderVO implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ACTIVITY_ORDER_ID")
	private Integer activityOrderId;

	@NotNull(message = "請選擇活動")
	@Min(value = 1, message = "請選擇活動")
	@Column(name = "ACTIVITY_ID", nullable = false)
	private Integer activityId;

	@NotNull(message = "請輸入會員ID")
	@Min(value = 1, message = "會員ID不可小於1")
	@Column(name = "MEMBER_ID", nullable = false)
	private Integer memberId;

	@NotNull(message = "請輸入訂單金額")
	@Min(value = 0, message = "金額不可小於0")
	@Column(name = "ORDER_TOTAL", nullable = false)
	private Integer orderTotal = 0;

	@NotNull(message = "請選擇付款狀態")
	@Column(name = "PAYMENT_STATUS", nullable = false)
	private Integer paymentStatus = 0;

	@Column(name = "CREATED_AT", updatable = false)
	private LocalDateTime createdAt;

	@PrePersist
	protected void onCreate() {
		this.createdAt = LocalDateTime.now();
	}

	// Getter & Setter

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