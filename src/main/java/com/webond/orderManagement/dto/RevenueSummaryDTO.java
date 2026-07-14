package com.webond.orderManagement.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class RevenueSummaryDTO {
    private BigDecimal serviceRevenue;
    private BigDecimal activityRevenue;
    private BigDecimal venueRevenue;
    private BigDecimal totalRevenue;
    private LocalDateTime rangeStart;
    private LocalDateTime rangeEnd;
    
    public RevenueSummaryDTO(BigDecimal serviceRevenue, BigDecimal activityRevenue, BigDecimal venueRevenue, BigDecimal total, LocalDateTime start, LocalDateTime end) {
    	super();
        this.serviceRevenue = serviceRevenue;
        this.activityRevenue = activityRevenue;
        this.venueRevenue = venueRevenue;
        this.totalRevenue = total;
        this.rangeStart = start;
        this.rangeEnd = end;
    }
    
    public RevenueSummaryDTO() {
    	super();
    }
    
	public BigDecimal getServiceRevenue() {
		return serviceRevenue;
	}
	public void setServiceRevenue(BigDecimal serviceRevenue) {
		this.serviceRevenue = serviceRevenue;
	}
	public BigDecimal getActivityRevenue() {
		return activityRevenue;
	}
	public void setActivityRevenue(BigDecimal activityRevenue) {
		this.activityRevenue = activityRevenue;
	}
	public BigDecimal getVenueRevenue() {
		return venueRevenue;
	}
	public void setVenueRevenue(BigDecimal venueRevenue) {
		this.venueRevenue = venueRevenue;
	}
	public BigDecimal getTotalRevenue() {
		return totalRevenue;
	}
	public void setTotalRevenue(BigDecimal totalRevenue) {
		this.totalRevenue = totalRevenue;
	}
	public LocalDateTime getRangeStart() {
		return rangeStart;
	}
	public void setRangeStart(LocalDateTime rangeStart) {
		this.rangeStart = rangeStart;
	}
	public LocalDateTime getRangeEnd() {
		return rangeEnd;
	}
	public void setRangeEnd(LocalDateTime rangeEnd) {
		this.rangeEnd = rangeEnd;
	}
    
    

}
