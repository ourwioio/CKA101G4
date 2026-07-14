package com.webond.orderManagement.dto;

import java.time.LocalDateTime;

public class RefundPayoutQueryDTO {

	private LocalDateTime startDate;
	private LocalDateTime endDate;
	private String quickRange; // thisMonth, thisQuarter, thisYear, all, custom
	private String sourceType; // ONE_ON_ONE, GROUP, VENUE, null=全部

	public void resolveRange() {
		if (quickRange == null)
			return;
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

	public String getSourceType() {
		return sourceType;
	}

	public void setSourceType(String sourceType) {
		this.sourceType = sourceType;
	}
}
