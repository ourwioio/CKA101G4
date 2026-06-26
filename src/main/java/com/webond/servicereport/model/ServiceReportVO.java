package com.webond.servicereport.model;

import java.sql.Timestamp;

import com.webond.employee.model.EmployeeVO;
import com.webond.member.model.MemberVO;
import com.webond.serviceorder.model.ServiceOrderVO;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "SERVICE_REPORT")

public class ServiceReportVO implements java.io.Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "SERVICE_REPORT_ID")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer serviceReportId;

	@OneToOne
	@JoinColumn(name = "SERVICE_ORDER_ID", referencedColumnName = "SERVICE_ORDER_ID")
	private ServiceOrderVO serviceOrder;

	@ManyToOne
	@JoinColumn(name = "REPORTER_MEMBER_ID", referencedColumnName = "MEMBER_ID")
	private MemberVO reporterMember;
	
	@ManyToOne
	@JoinColumn(name = "EMPLOYEE_ID", referencedColumnName = "EMPLOYEE_ID")
	private EmployeeVO employee;

	@Column(name = "SERVICE_REPORT_COM")
	private String serviceReportCom;

	@Column(name = "SERVICE_REPORT_TIME")
	private Timestamp serviceReportTime;

	@Column(name = "SERVICE_REPORT_HANDLE_TIME")
	private Timestamp serviceReportHandleTime;

	@Column(name = "SERVICE_REPORT_STATUS", columnDefinition = "byte default 0")
	private Byte serviceReportStatus;

	@Column(name = "REPORT_RESULT_ROLE")
	private Byte reportResultRole;

	public Integer getServiceReportId() {
		return serviceReportId;
	}

	public void setServiceReportId(Integer serviceReportId) {
		this.serviceReportId = serviceReportId;
	}

	public ServiceOrderVO getServiceOrder() {
		return serviceOrder;
	}

	public void setServiceOrder(ServiceOrderVO serviceOrder) {
		this.serviceOrder = serviceOrder;
	}

	public MemberVO getReporterMember() {
		return reporterMember;
	}

	public void setReporterMember(MemberVO reporterMember) {
		this.reporterMember = reporterMember;
	}

	public String getServiceReportCom() {
		return serviceReportCom;
	}

	public void setServiceReportCom(String serviceReportCom) {
		this.serviceReportCom = serviceReportCom;
	}

	public Timestamp getServiceReportTime() {
		return serviceReportTime;
	}

	public void setServiceReportTime(Timestamp serviceReportTime) {
		this.serviceReportTime = serviceReportTime;
	}

	public EmployeeVO getEmployee() {
		return employee;
	}

	public void setEmployee(EmployeeVO employee) {
		this.employee = employee;
	}

	public Timestamp getServiceReportHandleTime() {
		return serviceReportHandleTime;
	}

	public void setServiceReportHandleTime(Timestamp serviceReportHandleTime) {
		this.serviceReportHandleTime = serviceReportHandleTime;
	}

	public Byte getServiceReportStatus() {
		return serviceReportStatus;
	}

	public void setServiceReportStatus(Byte serviceReportStatus) {
		this.serviceReportStatus = serviceReportStatus;
	}

	public Byte getReportResultRole() {
		return reportResultRole;
	}

	public void setReportResultRole(Byte reportResultRole) {
		this.reportResultRole = reportResultRole;
	}

}
