package com.webond.platform.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "BULLETIN")
public class BulletinVO implements java.io.Serializable {

	private static final long serialVersionUID = 1L;

	@Id // @Id代表這個屬性是這個Entity的唯一識別屬性，並且對映到Table的主鍵
	@GeneratedValue(strategy = GenerationType.IDENTITY) // 自動遞增 (AUTO_INCREMENT)
	@Column(name = "BULLETIN_ID", updatable = false)
	private Integer bulletinId;

	@Column(name = "TITLE")
	@NotEmpty(message = "公告標題：請勿空白")
	@Size(max = 50, message = "公告標題：長度不能超過 {max} 個字元")
	private String title;

	@Column(name = "CONTENT")
	@Size(max = 500, message = "公告內容：長度不能超過 {max} 個字元")
	private String content;

	@Column(name = "STATUS")
	private Byte status = (byte) 0; // 預設草稿

	// 草稿時為 null，第一次發布時由 Service 層寫入，之後不再更動
	@Column(name = "PUBLISH_DATE")
	private LocalDate publishDate;

	@Column(name = "EMPLOYEE_ID")
	@NotNull(message = "請選擇負責員工")
	private Integer employeeId;

	@Column(name = "CREATED_AT", insertable = false, updatable = false)
	private LocalDateTime createdAt;

	@Column(name = "UPDATED_AT", insertable = false, updatable = false)
	private LocalDateTime updatedAt;

	@Column(name = "TAGS")
	@Size(max = 50, message = "標籤：長度不能超過 {max} 個字元")
	private String tags;

	public BulletinVO() {
		super();
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

	public LocalDate getPublishDate() {
		return publishDate;
	}

	public void setPublishDate(LocalDate publishDate) {
		this.publishDate = publishDate;
	}

	public Integer getEmployeeId() {
		return employeeId;
	}

	public void setEmployeeId(Integer employeeId) {
		this.employeeId = employeeId;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(LocalDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}

	public String getTags() {
		return tags;
	}

	public void setTags(String tags) {
		this.tags = tags;
	}
}