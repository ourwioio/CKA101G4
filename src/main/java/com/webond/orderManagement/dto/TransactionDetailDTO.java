package com.webond.orderManagement.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TransactionDetailDTO {

    private String orderType;        // "服務" / "活動" / "場地"
    private Integer orderId;
    private BigDecimal totalAmount;
    private Byte orderStatus;
    private String statusLabel;      // 中文顯示狀態
    private LocalDateTime transactionTime; // 服務/場地用 createdAt，活動用 paidAt
    private Boolean isRevenue;       // 是否算入收入
    private String category;

    public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public TransactionDetailDTO() {
    }

    public String getOrderType() {
        return orderType;
    }

    public void setOrderType(String orderType) {
        this.orderType = orderType;
    }

    public Integer getOrderId() {
        return orderId;
    }

    public void setOrderId(Integer orderId) {
        this.orderId = orderId;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public Byte getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(Byte orderStatus) {
        this.orderStatus = orderStatus;
    }

    public String getStatusLabel() {
        return statusLabel;
    }

    public void setStatusLabel(String statusLabel) {
        this.statusLabel = statusLabel;
    }

    public LocalDateTime getTransactionTime() {
        return transactionTime;
    }

    public void setTransactionTime(LocalDateTime transactionTime) {
        this.transactionTime = transactionTime;
    }

    public Boolean getIsRevenue() {
        return isRevenue;
    }

    public void setIsRevenue(Boolean isRevenue) {
        this.isRevenue = isRevenue;
    }
}