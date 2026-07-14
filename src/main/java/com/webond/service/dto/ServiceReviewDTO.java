package com.webond.service.dto;

import java.time.LocalDateTime;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class ServiceReviewDTO {

    @NotNull(message = "訂單編號不可為空")
    private Integer orderId;

    @NotNull(message = "請選擇評分")
    @Min(value = 1, message = "評分最低為 1")
    @Max(value = 5, message = "評分最高為 5")
    private Byte rate;

    @Size(max = 500, message = "評論內容不可超過 500 字")
    private String comment;
	@DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
	private LocalDateTime reviewAt;
	
	public ServiceReviewDTO() {
		super();
	}

	public Integer getOrderId() {
		return orderId;
	}

	public void setOrderId(Integer orderId) {
		this.orderId = orderId;
	}

	public Byte getRate() {
		return rate;
	}

	public void setRate(Byte rate) {
		this.rate = rate;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public LocalDateTime getReviewAt() {
		return reviewAt;
	}

	public void setReviewAt(LocalDateTime reviewAt) {
		this.reviewAt = reviewAt;
	}
	
	

}
