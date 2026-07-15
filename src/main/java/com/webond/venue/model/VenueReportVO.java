package com.webond.venue.model;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "VENUE_REPORT")
public class VenueReportVO implements java.io.Serializable {

	private static final long serialVersionUID = 1L;

	@Id // @Id代表這個屬性是這個Entity的唯一識別屬性，並且對映到Table的主鍵
	@GeneratedValue(strategy = GenerationType.IDENTITY) // 自動遞增 (AUTO_INCREMENT)
	@Column(name = "VENUE_REPORT_ID", updatable = false)
	private Integer venueReportId;

	@NotNull(message = "場地訂單編號：請勿空白")
	@Column(name = "VENUE_ORDER_ID")
	private Integer venueOrderId;

	@NotBlank(message = "檢舉內容：請勿空白")
	@Size(max = 500, message = "檢舉內容：長度不可超過 500 字")
	@Column(name = "SER_REPORT_COM")
	private String serReportCom;

	@Column(name = "SER_REPORT_TIME")
	private LocalDateTime serReportTime;

	@Column(name = "REPORT_STATUS")
	private Byte reportStatus;

	@Column(name = "EMPLOYEE_ID")
	private Integer employeeId;

	@Column(name = "HANDLED_AT")
	private LocalDateTime handledAt;

	public VenueReportVO() {
		super();
	}

	public Integer getVenueReportId() {
		return venueReportId;
	}

	public void setVenueReportId(Integer venueReportId) {
		this.venueReportId = venueReportId;
	}

	public Integer getVenueOrderId() {
		return venueOrderId;
	}

	public void setVenueOrderId(Integer venueOrderId) {
		this.venueOrderId = venueOrderId;
	}

	public String getSerReportCom() {
		return serReportCom;
	}

	public void setSerReportCom(String serReportCom) {
		this.serReportCom = serReportCom;
	}

	public LocalDateTime getSerReportTime() {
		return serReportTime;
	}

	public void setSerReportTime(LocalDateTime serReportTime) {
		this.serReportTime = serReportTime;
	}

	public Byte getReportStatus() {
		return reportStatus;
	}

	public void setReportStatus(Byte reportStatus) {
		this.reportStatus = reportStatus;
	}

	public Integer getEmployeeId() {
		return employeeId;
	}

	public void setEmployeeId(Integer employeeId) {
		this.employeeId = employeeId;
	}

	public LocalDateTime getHandledAt() {
		return handledAt;
	}

	public void setHandledAt(LocalDateTime handledAt) {
		this.handledAt = handledAt;
	}
}