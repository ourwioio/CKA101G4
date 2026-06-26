package com.webond.bulletin.model;

import java.util.Date;

public class BulletinVO implements java.io.Serializable{

	private Integer bulletinId;
	private String title;
	private String content;
	private Byte status;
	private Date publishDate;
	private Integer employeeId;
	private Date createdAt;
	private Date updatedAt;
	private String tags;
	
	public BulletinVO() {
		super();
	}
	
	public BulletinVO(Integer bulletinId, String title, String content, Byte status, 
			Date publishDate, Integer employeeId, Date createdAt, Date updatedAt, String tags) {
		super();
		this.bulletinId = bulletinId;
		this.title = title;
		this.content = content;
		this.status = status;
		this.publishDate = publishDate;
		this.employeeId = employeeId;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
		this.tags = tags;
	}
	
	public Integer getBulletinId() {
		return bulletinId;
	}
	public void setBulletinId(Integer bulletinId) {
		this.bulletinId = bulletinId;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public Byte getStatus() {
		return status;
	}
	public void setStatus(Byte status) {
		this.status = status;
	}
	public Date getPublishDate() {
		return publishDate;
	}
	public void setPublishDate(Date publishDate) {
		this.publishDate = publishDate;
	}
	public Integer getEmployeeId() {
		return employeeId;
	}
	public void setEmployeeId(Integer employeeId) {
		this.employeeId = employeeId;
	}
	public Date getCreatedAt() {
		return createdAt;
	}
	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}
	public Date getUpdatedAt() {
		return updatedAt;
	}
	public void setUpdatedAt(Date updatedAt) {
		this.updatedAt = updatedAt;
	}
	public String getTags() {
		return tags;
	}
	public void setTags(String tags) {
		this.tags = tags;
	}

	@Override
	public String toString() {
		return "BulletinVO [bulletinId=" + bulletinId + ", title=" + title + ", content=" + content + ", status="
				+ status + ", publishDate=" + publishDate + ", employeeId=" + employeeId + ", createdAt=" + createdAt
				+ ", updatedAt=" + updatedAt + ", tags=" + tags + "]";
	}

	

}
