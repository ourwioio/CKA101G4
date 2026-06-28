package com.webond.employee.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.sql.Timestamp;


@Entity
@Table(name = "employee_permission")
public class EmpPermVO {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "EMP_PERM_ID", updatable = false)
	private Integer empPermId;  

	@ManyToOne
	@JoinColumn(name = "EMPLOYEE_ID", referencedColumnName = "EMPLOYEE_ID")
	private EmpVO empId;  		 
	
	@ManyToOne
	@JoinColumn(name = "PERMISSION_ID", referencedColumnName = "PERMISSION_ID")
	private PermissionVO permId;  
	
	@Column(name = "ASSIGNED_AT")
	private Timestamp assignedAt;  

	public EmpPermVO() {
		super();
	}

	public EmpPermVO(Integer empPermId, EmpVO empId, PermissionVO permId, Timestamp assignedAt) {
		super();
		this.empPermId = empPermId;
		this.empId = empId;
		this.permId = permId;
		this.assignedAt = assignedAt;
	}

	public Integer getEmpPermId() {
		return empPermId;
	}

	public void setEmpPermId(Integer empPermId) {
		this.empPermId = empPermId;
	}

	public EmpVO getEmpId() {
		return empId;
	}

	public void setEmpId(EmpVO empId) {
		this.empId = empId;
	}

	public PermissionVO getPermId() {
		return permId;
	}

	public void setPermId(PermissionVO permId) {
		this.permId = permId;
	}

	public Timestamp getAssignedAt() {
		return assignedAt;
	}

	public void setAssignedAt(Timestamp assignedAt) {
		this.assignedAt = assignedAt;
	}
	
	
	
	
}
