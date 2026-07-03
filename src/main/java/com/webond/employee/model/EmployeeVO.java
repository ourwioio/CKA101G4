package com.webond.employee.model;

import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;

import com.webond.service.model.ServiceReportVO;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Pattern;

@Entity
@Table(name = "EMPLOYEE")
public class EmployeeVO implements java.io.Serializable{
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Pattern(regexp = "^[(\u4e00-\u9fa5)(a-zA-Z0-9_)]{2,10}$", message = "員工姓名: 只能是中、英文字母、數字和_ , 且長度必需在2到10之間")
	@Column(name = "EMPLOYEE_ID", updatable = false)
	private Integer employeeId;
	
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
	private Timestamp createdAt;
	
	@Column(name = "UPDATED_AT")
	private Timestamp updatedAt;
	
	@Column(name = "LAST_LOGIN_AT")
	private Timestamp lastLoginAt;
	
	@Column(name = "EMP_IMG", columnDefinition = "longblob")
	private byte[] empImg;
	
	@OneToMany(mappedBy = "emps", fetch = FetchType.EAGER)
	private Set<EmpPermVO> empPermVO = new HashSet<>();
	
	@OneToMany(cascade = CascadeType.ALL, mappedBy = "employee")
	@OrderBy("serviceReportId")
	private Set<ServiceReportVO> serviceReports;


	public EmployeeVO() {
		super();
	}


	public EmployeeVO(
			Integer employeeId,String empAccount, String empPassword, String empName, String roleTitle, Integer empStatus,
			Timestamp createdAt, Timestamp updatedAt, Timestamp lastLoginAt, byte[] empImg, Set<EmpPermVO> empPermVO) {
		this.employeeId = employeeId;
		this.empAccount = empAccount;
		this.empPassword = empPassword;
		this.empName = empName;
		this.roleTitle = roleTitle;
		this.empStatus = empStatus;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
		this.lastLoginAt = lastLoginAt;
		this.empImg = empImg;
		this.empPermVO = empPermVO;
	}


	public Integer getEmployeeId() {
		return employeeId;
	}

	public void setEmployeeId(Integer employeeId) {
		this.employeeId = employeeId;
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

	public Timestamp getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Timestamp createdAt) {
		this.createdAt = createdAt;
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

	public Set<EmpPermVO> getEmpPermVO() {
		return empPermVO;
	}

	public void setEmpPermVO(Set<EmpPermVO> empPermVO) {
		this.empPermVO = empPermVO;
	}


	public Set<ServiceReportVO> getServiceReports() {
		return serviceReports;
	}


	public void setServiceReports(Set<ServiceReportVO> serviceReports) {
		this.serviceReports = serviceReports;
	}
	
	
	
	
}
