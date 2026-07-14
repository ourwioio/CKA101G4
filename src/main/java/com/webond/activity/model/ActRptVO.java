package com.webond.activity.model;

import java.sql.Timestamp;

import com.webond.employee.model.EmployeeVO;

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
	@JoinColumn(name = "ACTIVITY_ORDER_ID", referencedColumnName = "ACTIVITY_ORDER_ID")
	private ActivityOrderVO actOrdId;	
	
	@ManyToOne
	@JoinColumn(name = "EMPLOYEE_ID", referencedColumnName = "EMPLOYEE_ID")
	private EmployeeVO empId;		
	
	@Column(name = "ACTIVITY_REPORT_TIME", updatable = false)
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
	
	@Column(name = "APPEAL_TIME", updatable = false)
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

	public ActRptVO(Integer actRptId, ActivityOrderVO actOrdId, EmployeeVO empId, Timestamp actRptTime, Timestamp updated,
			Integer rptType, String actRptCom, byte[] actRptImg, String appealContent, byte[] appealImg,
			Timestamp appealTime, Integer actRptStatus, Integer penaltyType, Integer penaltyValue, String remark) {
		super();
		this.actRptId = actRptId;
		this.actOrdId = actOrdId;
		this.empId = empId;
		this.actRptTime = actRptTime;
		this.updated = updated;
		this.rptType = rptType;
		this.actRptCom = actRptCom;
		this.actRptImg = actRptImg;
		this.appealContent = appealContent;
		this.appealImg = appealImg;
		this.appealTime = appealTime;
		this.actRptStatus = actRptStatus;
		this.penaltyType = penaltyType;
		this.penaltyValue = penaltyValue;
		this.remark = remark;
	}

	public Integer getActRptId() {
		return actRptId;
	}

	public void setActRptId(Integer actRptId) {
		this.actRptId = actRptId;
	}

	public ActivityOrderVO getActOrdId() {
		return actOrdId;
	}

	public void setActOrdId(ActivityOrderVO actOrdId) {
		this.actOrdId = actOrdId;
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
