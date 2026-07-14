package com.webond.orderManagement.dto;

import java.time.LocalDateTime;

public class RevenueQueryDTO {
	private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String quickRange; // "thisMonth", "thisQuarter", "thisYear", "all", "custom"
	private String orderType;    // 服務、活動、場地
    private Byte orderStatus; // 0, 1, 2, 3, 4, 5...

    // 依 quickRange 換算成 startDate/endDate
    public void resolveRange() {
        LocalDateTime now = LocalDateTime.now();
        switch (quickRange) {
            case "thisMonth":
                startDate = now.withDayOfMonth(1).toLocalDate().atStartOfDay();
                endDate = now;
                break;
            case "thisQuarter":
                int currentQuarterMonth = ((now.getMonthValue() - 1) / 3) * 3 + 1;
                startDate = now.withMonth(currentQuarterMonth).withDayOfMonth(1).toLocalDate().atStartOfDay();
                endDate = now;
                break;
            case "thisYear":
                startDate = now.withDayOfYear(1).toLocalDate().atStartOfDay();
                endDate = now;
                break;
            case "all":
                startDate = null;
                endDate = null;
                break;
            case "custom":
                break;
        }
    }

	public LocalDateTime getStartDate() {
		return startDate;
	}

	public void setStartDate(LocalDateTime startDate) {
		this.startDate = startDate;
	}

	public LocalDateTime getEndDate() {
		return endDate;
	}

	public void setEndDate(LocalDateTime endDate) {
		this.endDate = endDate;
	}

	public String getQuickRange() {
		return quickRange;
	}

	public void setQuickRange(String quickRange) {
		this.quickRange = quickRange;
	}
    public String getOrderType() {
		return orderType;
	}

	public void setOrderType(String orderType) {
		this.orderType = orderType;
	}

	public Byte getOrderStatus() {
		return orderStatus;
	}

	public void setOrderStatus(Byte orderStatus) {
		this.orderStatus = orderStatus;
	}
    

}
