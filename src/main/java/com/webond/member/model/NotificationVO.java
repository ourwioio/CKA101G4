package com.webond.member.model;

import java.time.LocalDate;
import java.util.Set;

import com.webond.employee.model.EmployeeVO;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotEmpty;

@Entity
@Table(name = "NOTIFICATION")

public class NotificationVO implements java.io.Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "NOTIFICATION_ID")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer notificationId;

	@ManyToOne
	@JoinColumn(name = "MEMBER_ID", referencedColumnName = "MEMBER_ID")
	private MemberVO member;

	@ManyToOne
	@JoinColumn(name = "REPORT_ID", referencedColumnName = "REPORT_ID")
	private MemberReportVO report;

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "memberReport")
	@OrderBy("notificationId asc")
	private Set<NotificationVO> notifications;

	@ManyToOne
	@JoinColumn(name = "EMPLOYEE_ID", referencedColumnName = "EMPLOYEE_ID")
	private EmployeeVO employee;

//	@OneToMany(cascade = CascadeType.ALL, mappedBy = "employee")
//	@OrderBy("notificationId asc")
//	private Set<NotificationVO> notifications;

	public Integer getNotificationId() {
		return notificationId;
	}

	@Column(name = "TITLE")
	@NotEmpty(message = "請輸入標題")
	private String title;

	@Column(name = "CONTENT")
	@NotEmpty(message = "請輸入內容")
	private String content;

	@Column(name = "NOTIFICATION_TYPE")
	@NotEmpty(message = "請選擇類型")
	private Byte notificationType;

	@Column(name = "IS_READ", columnDefinition = "byte default 0")
	private Byte isRead;

	@Column(name = "CREATED_AT")
	private LocalDate createdAt;

	public NotificationVO() {
		super();
	}

	public void setNotificationId(Integer notificationId) {
		this.notificationId = notificationId;
	}

	public MemberVO getMember() {
		return member;
	}

	public void setMember(MemberVO member) {
		this.member = member;
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

	public Byte getNotificationType() {
		return notificationType;
	}

	public void setNotificationType(Byte notificationType) {
		this.notificationType = notificationType;
	}

	public Byte getIsRead() {
		return isRead;
	}

	public void setIsRead(Byte isRead) {
		this.isRead = isRead;
	}

	public LocalDate getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDate createdAt) {
		this.createdAt = createdAt;
	}

	public MemberReportVO getReport() {
		return report;
	}

	public void setReport(MemberReportVO report) {
		this.report = report;
	}

	public EmployeeVO getEmployee() {
		return employee;
	}

	public void setEmployee(EmployeeVO employee) {
		this.employee = employee;
	}

}
