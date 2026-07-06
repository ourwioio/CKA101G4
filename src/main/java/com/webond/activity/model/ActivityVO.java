package com.webond.activity.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import org.springframework.format.annotation.DateTimeFormat;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

@Entity
@Table(name = "ACTIVITY")
public class ActivityVO implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ACTIVITY_ID")
	private Integer activityId;

	@NotNull(message = "請選擇活動類型")
	@Column(name = "ACTIVITY_TYPE_ID")
	private Integer activityTypeId;

	@NotNull(message = "請輸入主辦會員ID")
	@Min(value = 1, message = "會員ID不可小於1")
	@Column(name = "MEMBER_ID")
	private Integer memberId;

	@NotBlank(message = "請輸入活動標題")
	@Column(name = "ACTIVITY_TITLE")
	private String activityTitle;

	@NotBlank(message = "請輸入活動描述")
	@Column(name = "ACTIVITY_DESCRIPTION")
	private String activityDescription;

	@NotNull(message = "請輸入活動費用")
	@PositiveOrZero(message = "活動費用不可小於0")
	@Column(name = "ACTIVITY_PRICE")
	private Integer activityPrice;

	@NotNull(message = "請輸入最低成團人數")
	@Min(value = 1, message = "最低成團人數至少1人")
	@Column(name = "MIN_PARTICIPANTS")
	private Integer minParticipants;

	@NotNull(message = "請輸入最高人數")
	@Min(value = 1, message = "最高人數至少1人")
	@Column(name = "MAX_PARTICIPANTS")
	private Integer maxParticipants;

	@Column(name = "ATTENDEES_COUNT")
	private Integer attendeesCount;

	@NotNull(message = "請選擇報名開始時間")
	@DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
	@Column(name = "REGISTRATION_STARTTIME")
	private LocalDateTime registrationStartTime;

	@NotNull(message = "請選擇報名截止時間")
	@DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
	@Column(name = "REGISTRATION_DEADLINE")
	private LocalDateTime registrationDeadline;

	@NotNull(message = "請選擇活動狀態")
	@Column(name = "ACTIVITY_STATUS")
	private Byte activityStatus;

	@Column(name = "CREATED_AT", insertable = false, updatable = false)
	private LocalDateTime createdAt;

	@NotNull(message = "請選擇活動開始時間")
	@DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
	@Column(name = "START_TIME")
	private LocalDateTime startTime;

	@NotNull(message = "請選擇活動結束時間")
	@DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
	@Column(name = "END_TIME")
	private LocalDateTime endTime;

	// ================= Getter / Setter =================

	public Integer getActivityId() {
		return activityId;
	}

	public void setActivityId(Integer activityId) {
		this.activityId = activityId;
	}

	public Integer getActivityTypeId() {
		return activityTypeId;
	}

	public void setActivityTypeId(Integer activityTypeId) {
		this.activityTypeId = activityTypeId;
	}

	public Integer getMemberId() {
		return memberId;
	}

	public void setMemberId(Integer memberId) {
		this.memberId = memberId;
	}

	public String getActivityTitle() {
		return activityTitle;
	}

	public void setActivityTitle(String activityTitle) {
		this.activityTitle = activityTitle;
	}

	public String getActivityDescription() {
		return activityDescription;
	}

	public void setActivityDescription(String activityDescription) {
		this.activityDescription = activityDescription;
	}

	public Integer getActivityPrice() {
		return activityPrice;
	}

	public void setActivityPrice(Integer activityPrice) {
		this.activityPrice = activityPrice;
	}

	public Integer getMinParticipants() {
		return minParticipants;
	}

	public void setMinParticipants(Integer minParticipants) {
		this.minParticipants = minParticipants;
	}

	public Integer getMaxParticipants() {
		return maxParticipants;
	}

	public void setMaxParticipants(Integer maxParticipants) {
		this.maxParticipants = maxParticipants;
	}

	public Integer getAttendeesCount() {
		return attendeesCount;
	}

	public void setAttendeesCount(Integer attendeesCount) {
		this.attendeesCount = attendeesCount;
	}

	public LocalDateTime getRegistrationStartTime() {
		return registrationStartTime;
	}

	public void setRegistrationStartTime(LocalDateTime registrationStartTime) {
		this.registrationStartTime = registrationStartTime;
	}

	public LocalDateTime getRegistrationDeadline() {
		return registrationDeadline;
	}

	public void setRegistrationDeadline(LocalDateTime registrationDeadline) {
		this.registrationDeadline = registrationDeadline;
	}

	public Byte getActivityStatus() {
		return activityStatus;
	}

	public void setActivityStatus(Byte activityStatus) {
		this.activityStatus = activityStatus;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public LocalDateTime getStartTime() {
		return startTime;
	}

	public void setStartTime(LocalDateTime startTime) {
		this.startTime = startTime;
	}

	public LocalDateTime getEndTime() {
		return endTime;
	}

	public void setEndTime(LocalDateTime endTime) {
		this.endTime = endTime;
	}
}
