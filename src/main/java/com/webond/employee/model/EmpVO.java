package com.webond.employee.model;

import java.sql.Timestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "EMPLOYEE")
public class EmpVO {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "EMPLOYEE_ID", updatable = false)
	private Integer empId;
	
	@Column(name = "EMP_ACCOUNT", unique = true, nullable = false)
	private String empAccount;
	
	@Column(name = "PASSWORD_HASH", nullable = false)
	private String empPassword;
	
	@Column(name = "EMP_NAME")
	private String empName;
	
	@Column(name = "ROLE_TITLE")
	private String roleTitle;
	
	@Column(name = "EMP_STATUS")
	private Integer empStatus;
	
	@Column(name = "CREATED_AT", updatable = false)
	private Timestamp createAt;
	
	@Column(name = "UPDATED_AT")
	private Timestamp updatedAt;
	
	@Column(name = "LAST_LOGIN_AT")
	private Timestamp lastLoginAt;
	
	@Column(name = "EMP_IMG", columnDefinition = "longblob")
	private byte[] empImg;

	
	

	public EmpVO() {
		super();
	}
	

	public EmpVO(Integer empId, String empAccount, String empPassword, String empName, String roleTitle,
			Integer empStatus, Timestamp createAt, Timestamp updatedAt, Timestamp lastLoginAt, byte[] empImg) {
		super();
		this.empId = empId;
		this.empAccount = empAccount;
		this.empPassword = empPassword;
		this.empName = empName;
		this.roleTitle = roleTitle;
		this.empStatus = empStatus;
		this.createAt = createAt;
		this.updatedAt = updatedAt;
		this.lastLoginAt = lastLoginAt;
		this.empImg = empImg;
	}



	public Integer getEmpId() {
		return empId;
	}

	public void setEmpId(Integer empId) {
		this.empId = empId;
	}

	public String getEmpAccount() {
		return empAccount;
	}

	public void setEmpAccount(String empAccount) {
		this.empAccount = empAccount;
	}

	public String getEmpPassword() {
		return empPassword;
	}

	public void setEmpPassword(String empPassword) {
		this.empPassword = empPassword;
	}

	public String getEmpName() {
		return empName;
	}

	public void setEmpName(String empName) {
		this.empName = empName;
	}

	public String getRoleTitle() {
		return roleTitle;
	}

	public void setRoleTitle(String roleTitle) {
		this.roleTitle = roleTitle;
	}

	public Integer getEmpStatus() {
		return empStatus;
	}

	public void setEmpStatus(Integer empStatus) {
		this.empStatus = empStatus;
	}

	public Timestamp getCreateAt() {
		return createAt;
	}

	public void setCreateAt(Timestamp createAt) {
		this.createAt = createAt;
	}

	public Timestamp getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(Timestamp updatedAt) {
		this.updatedAt = updatedAt;
	}

	public Timestamp getLastLoginAt() {
		return lastLoginAt;
	}

	public void setLastLoginAt(Timestamp lastLoginAt) {
		this.lastLoginAt = lastLoginAt;
	}

	public byte[] getEmpImg() {
		return empImg;
	}

	public void setEmpImg(byte[] empImg) {
		this.empImg = empImg;
	}
	
	
}
