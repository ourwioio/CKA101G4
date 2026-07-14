package com.webond.service.dto;

import java.time.LocalDateTime;

public class MemberReviewDTO {
    private Integer orderId;
    private String comment;
    private Byte rating;
    private LocalDateTime reviewedAt;
    private String role; // "AS_SELLER" 或 "AS_BUYER"，畫面上可以用來顯示「賣家評價」/「買家評價」

    public MemberReviewDTO(Integer orderId, String comment, Byte rating, LocalDateTime reviewedAt, String role) {
        this.orderId = orderId;
        this.comment = comment;
        this.rating = rating;
        this.reviewedAt = reviewedAt;
        this.role = role;
    }

    // getters ...
    public Integer getOrderId() { return orderId; }
    public String getComment() { return comment; }
    public Byte getRating() { return rating; }
    public LocalDateTime getReviewedAt() { return reviewedAt; }
    public String getRole() { return role; }
}
