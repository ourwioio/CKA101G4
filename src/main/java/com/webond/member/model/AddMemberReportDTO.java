package com.webond.member.model;

import org.springframework.web.multipart.MultipartFile;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class AddMemberReportDTO {
	@NotNull(message = "檢舉人編號請勿空白")
	private Integer reporterId;

	@NotNull(message = "被檢舉人編號請勿空白")
	private Integer reportedId;

	@NotNull(message = "請選擇檢舉原因")
	private Integer reportCategory;

	@NotBlank(message = "詳細內容描述請勿空白")
	private String reportContent;

	// 🎯 修改為陣列型態以接收多張上傳圖片
	private MultipartFile[] evidencePath; 

	public AddMemberReportDTO() {
    }

	// Getter / Setter
	public Integer getReporterId() {
		return reporterId;
	}

	public void setReporterId(Integer reporterId) {
		this.reporterId = reporterId;
	}

	public Integer getReportedId() {
		return reportedId;
	}

	public void setReportedId(Integer reportedId) {
		this.reportedId = reportedId;
	}

	public Integer getReportCategory() {
		return reportCategory;
	}

	public void setReportCategory(Integer reportCategory) {
		this.reportCategory = reportCategory;
	}

	public String getReportContent() {
		return reportContent;
	}

	public void setReportContent(String reportContent) {
		this.reportContent = reportContent;
	}

	public MultipartFile[] getEvidencePath() {
		return evidencePath;
	}

	public void setEvidencePath(MultipartFile[] evidencePath) {
		this.evidencePath = evidencePath;
	}
}