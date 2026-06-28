package com.webond.venue.model;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "VENUE_REVIEW")
public class VenueReviewVO implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    @Id //@Id代表這個屬性是這個Entity的唯一識別屬性，並且對映到Table的主鍵
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 自動遞增 (AUTO_INCREMENT)
    @Column(name = "VENUE_REVIEW_ID", updatable = false)
    private Integer venueReviewId;

    @Column(name = "VENUE_ID")
    @NotNull(message = "場地編號：請勿空白")
    private Integer venueId;

    @Column(name = "EMPLOYEE_ID")
    private Integer employeeId;

    @Column(name = "REVIEW_STATUS")
    private Byte reviewStatus = (byte) 0; // 預設審核中

    @Column(name = "REVIEW_NOTE")
    @Size(max = 255, message = "審核備註：長度不能超過 {max} 個字元")
    private String reviewNote;

    @Column(name = "REVIEWED_AT")
    private LocalDateTime reviewedAt;

    public VenueReviewVO() {
        super();
    }

    public Integer getVenueReviewId() {
        return venueReviewId;
    }

    public void setVenueReviewId(Integer venueReviewId) {
        this.venueReviewId = venueReviewId;
    }

    public Integer getVenueId() {
        return venueId;
    }

    public void setVenueId(Integer venueId) {
        this.venueId = venueId;
    }

    public Integer getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Integer employeeId) {
        this.employeeId = employeeId;
    }

    public Byte getReviewStatus() {
        return reviewStatus;
    }

    public void setReviewStatus(Byte reviewStatus) {
        this.reviewStatus = reviewStatus;
    }

    public String getReviewNote() {
        return reviewNote;
    }

    public void setReviewNote(String reviewNote) {
        this.reviewNote = reviewNote;
    }

    public LocalDateTime getReviewedAt() {
        return reviewedAt;
    }

    public void setReviewedAt(LocalDateTime reviewedAt) {
        this.reviewedAt = reviewedAt;
    }
}