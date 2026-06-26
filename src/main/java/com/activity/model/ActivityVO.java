package com.activity.model;

import java.io.Serializable;
import java.sql.Timestamp;
import jakarta.persistence.*; // 加上這個

@Entity // 宣告這是一個 Hibernate 實體
@Table(name = "ACTIVITY") // 對應資料庫的資料表名稱
public class ActivityVO implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id // 宣告這是不重複的主鍵 (Primary Key)
	@GeneratedValue(strategy = GenerationType.IDENTITY) // 自動遞增 (AUTO_INCREMENT)
	@Column(name = "ACTIVITY_ID")
	private Integer activityId;

	@Column(name = "ACTIVITY_TYPE_ID")
	private Integer activityTypeId;

	@Column(name = "MEMBER_ID")
	private Integer memberId;

	@Column(name = "ACTIVITY_TITLE")
	private String activityTitle;

	@Column(name = "ACTIVITY_DESCRIPTION")
	private String activityDescription;

	@Column(name = "ACTIVITY_PRICE")
	private Integer activityPrice;

	@Column(name = "MIN_PARTICIPANTS")
	private Integer minParticipants;

	@Column(name = "MAX_PARTICIPANTS")
	private Integer maxParticipants;

	@Column(name = "ATTENDEES_COUNT")
	private Integer attendeesCount;

	@Column(name = "REGISTRATION_STARTTIME")
	private Timestamp registrationStartTime;

	@Column(name = "REGISTRATION_DEADLINE")
	private Timestamp registrationDeadline;

	@Column(name = "ACTIVITY_STATUS")
	private Byte activityStatus;

	// insertable=false, updatable=false 讓這個欄位完全交給資料庫的預設值產生
	@Column(name = "CREATED_AT", insertable = false, updatable = false)
	private Timestamp createdAt;

	@Column(name = "END_TIME")
	private Timestamp endTime;

	// Getter and Setter
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

	public Timestamp getRegistrationStartTime() {
		return registrationStartTime;
	}

	public void setRegistrationStartTime(Timestamp registrationStartTime) {
		this.registrationStartTime = registrationStartTime;
	}

	public Timestamp getRegistrationDeadline() {
		return registrationDeadline;
	}

	public void setRegistrationDeadline(Timestamp registrationDeadline) {
		this.registrationDeadline = registrationDeadline;
	}

	public Byte getActivityStatus() {
		return activityStatus;
	}

	public void setActivityStatus(Byte activityStatus) {
		this.activityStatus = activityStatus;
	}

	public Timestamp getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Timestamp createdAt) {
		this.createdAt = createdAt;
	}

	public Timestamp getEndTime() {
		return endTime;
	}

	public void setEndTime(Timestamp endTime) {
		this.endTime = endTime;
	}
}