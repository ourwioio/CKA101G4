package com.webond.platform.model;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "PLATFORM_SPECIFICATION")
public class PlatformSpecificationVO implements java.io.Serializable {

	private static final long serialVersionUID = 1L;

	@Id // @Id代表這個屬性是這個Entity的唯一識別屬性，並且對映到Table的主鍵
	@GeneratedValue(strategy = GenerationType.IDENTITY) // 自動遞增 (AUTO_INCREMENT)
	@Column(name = "SPEC_ID", updatable = false)
	private Integer specId;

	@Column(name = "SPEC_TYPE")
	@NotNull(message = "請選擇平台規範類型")
	private Byte specType;

	@Column(name = "TITLE")
	@NotEmpty(message = "規範標題：請勿空白")
	@Size(max = 50, message = "規範標題：長度不能超過 {max} 個字元")
	private String title;

	@Column(name = "DESCRIPTION")
	@Size(max = 500, message = "規範內容：長度不能超過 {max} 個字元")
	private String description;

	@Column(name = "STATUS")
	private Byte status = (byte) 0; // 預設草稿

	@Column(name = "EMPLOYEE_ID")
	@NotNull(message = "請選擇負責員工")
	private Integer employeeId;

	@Column(name = "CREATED_AT", insertable = false, updatable = false)
	private LocalDateTime createdAt;

	@Column(name = "UPDATED_AT", insertable = false, updatable = false)
	private LocalDateTime updatedAt;

	public PlatformSpecificationVO() {
		super();
	}

	public Integer getSpecId() {
		return specId;
	}

	public void setSpecId(Integer specId) {
		this.specId = specId;
	}

	public Byte getSpecType() {
		return specType;
	}

	public void setSpecType(Byte specType) {
		this.specType = specType;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Byte getStatus() {
		return status;
	}

	public void setStatus(Byte status) {
		this.status = status;
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
}