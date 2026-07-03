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
	private EmployeeVO emps;  		  
	
	@ManyToOne
	@JoinColumn(name = "PERMISSION_ID", referencedColumnName = "PERMISSION_ID")
	private PermissionVO perms; 
	
	@Column(name = "ASSIGNED_AT")
	private Timestamp assignedAt; 

	public EmpPermVO() {
		super();
	}

	public EmpPermVO(Integer empPermId, EmployeeVO empId, PermissionVO permId, Timestamp assignedAt) {
		super();
		this.empPermId = empPermId;
		this.emps = empId;
		this.perms = permId;
		this.assignedAt = assignedAt;
	}

	public Integer getEmpPermId() {
		return empPermId;
	}

	public void setEmpPermId(Integer empPermId) {
		this.empPermId = empPermId;
	}

	public EmployeeVO getEmps() {
		return emps;
	}

	public void setEmps(EmployeeVO emps) {
		this.emps = emps;
	}

	public PermissionVO getPerms() {
		return perms;
	}

	public void setPerms(PermissionVO perms) {
		this.perms = perms;
	}

	public Timestamp getAssignedAt() {
		return assignedAt;
	}

	public void setAssignedAt(Timestamp assignedAt) {
		this.assignedAt = assignedAt;
	}
	
	
	
	
}
