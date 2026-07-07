package com.webond.venue.dto;

import java.time.LocalDate;

public class VenueOrderQueryDTO {
	
	private LocalDate createdAtStart;
    private LocalDate createdAtEnd;
    private Byte orderStatus;   
    private Byte payoutAmount;   
    private Byte refundStatus;   
    private Integer employeeId;  
    
	public VenueOrderQueryDTO() {
		super();
	}

	public LocalDate getCreatedAtStart() {
		return createdAtStart;
	}

	public void setCreatedAtStart(LocalDate createdAtStart) {
		this.createdAtStart = createdAtStart;
	}

	public LocalDate getCreatedAtEnd() {
		return createdAtEnd;
	}

	public void setCreatedAtEnd(LocalDate createdAtEnd) {
		this.createdAtEnd = createdAtEnd;
	}

	public Byte getOrderStatus() {
		return orderStatus;
	}

	public void setOrderStatus(Byte orderStatus) {
		this.orderStatus = orderStatus;
	}

	public Byte getPayoutAmount() {
		return payoutAmount;
	}

	public void setPayoutAmount(Byte payoutAmount) {
		this.payoutAmount = payoutAmount;
	}

	public Byte getRefundStatus() {
		return refundStatus;
	}

	public void setRefundStatus(Byte refundStatus) {
		this.refundStatus = refundStatus;
	}

	public Integer getEmployeeId() {
		return employeeId;
	}

	public void setEmployeeId(Integer employeeId) {
		this.employeeId = employeeId;
	}

    
}
