package com.webond.activity.model;

import java.sql.Timestamp;

import com.webond.employee.model.EmployeeVO;
import com.webond.member.model.MemberVO;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;


@Entity
@Table(name = "ACTIVITY_REPORT")
public class ActRptVO {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ACTIVITY_REPORT_ID", updatable = false)
	private Integer actRptId;		
	
	@ManyToOne
	@JoinColumn(name = "ACTIVITY_ID", referencedColumnName = "ACTIVITY_ID")
	private ActivityVO actId;
	
	@ManyToOne
	@JoinColumn(name = "REPORTER_ID", referencedColumnName = "MEMBER_ID")
	private MemberVO reporterId;
	
	@ManyToOne
	@JoinColumn(name = "EMPLOYEE_ID", referencedColumnName = "EMPLOYEE_ID")
	private EmployeeVO empId;		
	
	@Column(name = "ACTIVITY_REPORT_TIME")
	private Timestamp actRptTime;	
	
	@Column(name = "UPDATED")
	private Timestamp updated;	
	
	@Column(name = "REPORT_TYPE")
	private Integer rptType;		
	
	@Column(name = "ACTIVITY_REPORT_COM")
	private String actRptCom;	
	
	@Column(name = "ACTIVITY_REPORT_IMAGE", columnDefinition = "longblob")
	private byte[] actRptImg;		
	
	@Column(name = "APPEAL_CONTENT")
	private String appealContent;	
	
	@Column(name = "APPEAL_IMAGE", columnDefinition = "longblob")
	private byte[] appealImg;		
	
	@Column(name = "APPEAL_TIME")
	private Timestamp appealTime;	
	
	@Column(name = "ACTIVITY_REPORT_STATUS")
	private Integer actRptStatus;	

	@Column(name = "PENALTY_TYPE")
	private Integer penaltyType;   
	
	@Column(name = "PENALTY_VALUE" )
	private Integer penaltyValue;	
	
	@Column(name ="REMARK")
	private String remark;

	public ActRptVO() {
		super();
	}


	public Integer getActRptId() {
		return actRptId;
	}

	public void setActRptId(Integer actRptId) {
		this.actRptId = actRptId;
	}

	public ActivityVO getActId() {
		return actId;
	}

	public void setActId(ActivityVO actId) {
		this.actId = actId;
	}

	public MemberVO getReporterId() {
		return reporterId;
	}

	public void setReporterId(MemberVO reporterId) {
		this.reporterId = reporterId;
	}

	public EmployeeVO getEmpId() {
		return empId;
	}

	public void setEmpId(EmployeeVO empId) {
		this.empId = empId;
	}

	public Timestamp getActRptTime() {
		return actRptTime;
	}

	public void setActRptTime(Timestamp actRptTime) {
		this.actRptTime = actRptTime;
	}

	public Timestamp getUpdated() {
		return updated;
	}

	public void setUpdated(Timestamp updated) {
		this.updated = updated;
	}

	public Integer getRptType() {
		return rptType;
	}

	public void setRptType(Integer rptType) {
		this.rptType = rptType;
	}

	public String getActRptCom() {
		return actRptCom;
	}

	public void setActRptCom(String actRptCom) {
		this.actRptCom = actRptCom;
	}

	public byte[] getActRptImg() {
		return actRptImg;
	}

	public void setActRptImg(byte[] actRptImg) {
		this.actRptImg = actRptImg;
	}

	public String getAppealContent() {
		return appealContent;
	}

	public void setAppealContent(String appealContent) {
		this.appealContent = appealContent;
	}

	public byte[] getAppealImg() {
		return appealImg;
	}

	public void setAppealImg(byte[] appealImg) {
		this.appealImg = appealImg;
	}

	public Timestamp getAppealTime() {
		return appealTime;
	}

	public void setAppealTime(Timestamp appealTime) {
		this.appealTime = appealTime;
	}

	public Integer getActRptStatus() {
		return actRptStatus;
	}

	public void setActRptStatus(Integer actRptStatus) {
		this.actRptStatus = actRptStatus;
	}

	public Integer getPenaltyType() {
		return penaltyType;
	}

	public void setPenaltyType(Integer penaltyType) {
		this.penaltyType = penaltyType;
	}

	public Integer getPenaltyValue() {
		return penaltyValue;
	}

	public void setPenaltyValue(Integer penaltyValue) {
		this.penaltyValue = penaltyValue;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	} 	
	
	
	

	
	

}
