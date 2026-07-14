package com.webond.activity.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "ACTIVITY_TYPE")
public class ActivityTypeVO {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ACTIVITY_TYPE_ID")
	private Integer activityTypeId;

	@Column(name = "TYPE_NAME", nullable = false, length = 50)
	private String activityTypeName;

	@Column(name = "TYPE_MODE")
	private Byte typeMode = 1;

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

	public Byte getTypeMode() {
		return typeMode;
	}

	public void setTypeMode(Byte typeMode) {
		this.typeMode = typeMode;
	}
}
