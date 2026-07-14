package com.webond.activity.model;

import java.io.Serializable;
import java.time.LocalDateTime;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
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

	@NotNull(message = "\u8acb\u9078\u64c7\u6d3b\u52d5\u985e\u578b")
	@Column(name = "ACTIVITY_TYPE_ID")
	private Integer activityTypeId;

	@NotNull(message = "\u8acb\u63d0\u4f9b\u6d3b\u52d5\u767c\u8d77\u6703\u54e1\u7de8\u865f")
	@Min(value = 1, message = "\u6703\u54e1\u7de8\u865f\u4e0d\u53ef\u5c0f\u65bc 1")
	@Column(name = "MEMBER_ID")
	private Integer memberId;

	@NotBlank(message = "\u8acb\u8f38\u5165\u6d3b\u52d5\u6a19\u984c")
	@Column(name = "ACTIVITY_TITLE")
	private String activityTitle;

	@NotBlank(message = "\u8acb\u8f38\u5165\u6d3b\u52d5\u63cf\u8ff0")
	@Column(name = "ACTIVITY_DESCRIPTION")
	private String activityDescription;

	@Lob
	@Column(name = "ACTIVITY_IMAGE")
	private byte[] activityImage;

	@Column(name = "ACTIVITY_IMAGE_TYPE")
	private String activityImageType;

	@NotNull(message = "\u8acb\u8f38\u5165\u6d3b\u52d5\u50f9\u683c")
	@PositiveOrZero(message = "\u6d3b\u52d5\u50f9\u683c\u4e0d\u53ef\u5c0f\u65bc 0")
	@Column(name = "ACTIVITY_PRICE")
	private Integer activityPrice;

	@NotNull(message = "\u8acb\u8f38\u5165\u6700\u4f4e\u4eba\u6578")
	@Min(value = 1, message = "\u6700\u4f4e\u4eba\u6578\u81f3\u5c11\u70ba 1")
	@Column(name = "MIN_PARTICIPANTS")
	private Integer minParticipants;

	@NotNull(message = "\u8acb\u8f38\u5165\u6700\u9ad8\u4eba\u6578")
	@Min(value = 1, message = "\u6700\u9ad8\u4eba\u6578\u81f3\u5c11\u70ba 1")
	@Column(name = "MAX_PARTICIPANTS")
	private Integer maxParticipants;

	@Column(name = "ATTENDEES_COUNT")
	private Integer attendeesCount;

	@NotNull(message = "\u8acb\u9078\u64c7\u5831\u540d\u958b\u59cb\u6642\u9593")
	@DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
	@Column(name = "REGISTRATION_STARTTIME")
	private LocalDateTime registrationStartTime;

	@NotNull(message = "\u8acb\u9078\u64c7\u5831\u540d\u622a\u6b62\u6642\u9593")
	@DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
	@Column(name = "REGISTRATION_DEADLINE")
	private LocalDateTime registrationDeadline;

	@NotNull(message = "\u8acb\u9078\u64c7\u6d3b\u52d5\u72c0\u614b")
	@Column(name = "ACTIVITY_STATUS")
	private Byte activityStatus;

	@Column(name = "CREATED_AT", insertable = false, updatable = false)
	private LocalDateTime createdAt;

	@NotNull(message = "\u8acb\u9078\u64c7\u6d3b\u52d5\u958b\u59cb\u6642\u9593")
	@DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
	@Column(name = "START_TIME")
	private LocalDateTime startTime;

	@NotNull(message = "\u8acb\u9078\u64c7\u6d3b\u52d5\u7d50\u675f\u6642\u9593")
	@DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
	@Column(name = "END_TIME")
	private LocalDateTime endTime;

	public Integer getActivityId() { return activityId; }
	public void setActivityId(Integer activityId) { this.activityId = activityId; }
	public Integer getActivityTypeId() { return activityTypeId; }
	public void setActivityTypeId(Integer activityTypeId) { this.activityTypeId = activityTypeId; }
	public Integer getMemberId() { return memberId; }
	public void setMemberId(Integer memberId) { this.memberId = memberId; }
	public String getActivityTitle() { return activityTitle; }
	public void setActivityTitle(String activityTitle) { this.activityTitle = activityTitle; }
	public String getActivityDescription() { return activityDescription; }
	public void setActivityDescription(String activityDescription) { this.activityDescription = activityDescription; }
	public byte[] getActivityImage() { return activityImage; }
	public void setActivityImage(byte[] activityImage) { this.activityImage = activityImage; }
	public String getActivityImageType() { return activityImageType; }
	public void setActivityImageType(String activityImageType) { this.activityImageType = activityImageType; }
	public Integer getActivityPrice() { return activityPrice; }
	public void setActivityPrice(Integer activityPrice) { this.activityPrice = activityPrice; }
	public Integer getMinParticipants() { return minParticipants; }
	public void setMinParticipants(Integer minParticipants) { this.minParticipants = minParticipants; }
	public Integer getMaxParticipants() { return maxParticipants; }
	public void setMaxParticipants(Integer maxParticipants) { this.maxParticipants = maxParticipants; }
	public Integer getAttendeesCount() { return attendeesCount; }
	public void setAttendeesCount(Integer attendeesCount) { this.attendeesCount = attendeesCount; }
	public LocalDateTime getRegistrationStartTime() { return registrationStartTime; }
	public void setRegistrationStartTime(LocalDateTime registrationStartTime) { this.registrationStartTime = registrationStartTime; }
	public LocalDateTime getRegistrationDeadline() { return registrationDeadline; }
	public void setRegistrationDeadline(LocalDateTime registrationDeadline) { this.registrationDeadline = registrationDeadline; }
	public Byte getActivityStatus() { return activityStatus; }
	public void setActivityStatus(Byte activityStatus) { this.activityStatus = activityStatus; }
	public LocalDateTime getCreatedAt() { return createdAt; }
	public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
	public LocalDateTime getStartTime() { return startTime; }
	public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
	public LocalDateTime getEndTime() { return endTime; }
	public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
}
