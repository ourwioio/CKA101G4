package com.activity.model;

import jakarta.persistence.*;

@Entity
@Table(name = "ACTIVITY_TYPE") // 確保對應到你 MySQL 中的實際表格名稱
public class ActivityTypeVO {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ACTIVITY_TYPE_ID")
	private Integer activityTypeId;

	@Column(name = "ACTIVITY_TYPE_NAME", nullable = false, length = 50)
	private String activityTypeName;

	// Getters and Setters
	public Integer getActivityTypeId() {
		return activityTypeId;
	}

	public void setActivityTypeId(Integer activityTypeId) {
		this.activityTypeId = activityTypeId;
	}

	public String getActivityTypeName() {
		return activityTypeName;
	}

	public void setActivityTypeName(String activityTypeName) {
		this.activityTypeName = activityTypeName;
	}
}