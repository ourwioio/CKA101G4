package com.webond.employee.model;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import org.hibernate.annotations.CreationTimestamp;
import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.webond.employee.model.EmployeeVO.ValidGroup;
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
import jakarta.validation.GroupSequence;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

@GroupSequence({ ValidGroup.First.class, ValidGroup.Second.class, EmployeeVO.class })
@Entity
@Table(name = "EMPLOYEE")
public class EmployeeVO implements java.io.Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "EMPLOYEE_ID", updatable = false, unique = true, nullable = false)
	private Integer employeeId;

	@NotBlank(message = "帳號請勿空白", groups = ValidGroup.First.class)
	@Pattern(regexp = "^[a-zA-Z0-9]{5,20}@webond\\.com$", message = "帳號長度為 5 到 20 的英文或數字，@webond.com 結尾", groups = ValidGroup.Second.class)
	@Column(name = "EMP_ACCOUNT", unique = true, nullable = false)
	private String empAccount;

	@NotBlank(message = "密碼請勿空白", groups = ValidGroup.First.class)
	@Column(name = "PASSWORD_HASH", nullable = false)
	private String empPassword;

	@NotBlank(message = "姓名請勿空白", groups = ValidGroup.First.class)
	@Pattern(regexp = "^[\\u4e00-\\u9fa5a-zA-Z]{2,10}$", message = "姓名長度為 2 到 10 的中文或英文", groups = ValidGroup.Second.class)
	@Column(name = "EMP_NAME")
	private String empName;

	@NotNull(message = "職稱請勿空白", groups = ValidGroup.First.class)
	@Column(name = "ROLE_TITLE")
	private Integer roleTitle;

	@Column(name = "EMP_STATUS")
	private Integer empStatus;

	@CreationTimestamp
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
	@Column(name = "CREATED_AT", updatable = false)
	private Timestamp createdAt;

	@Column(name = "UPDATED_AT")
	private Timestamp updatedAt;

	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
	@Column(name = "LAST_LOGIN_AT")
	private LocalDateTime lastLoginAt;

	@Column(name = "EMP_IMG", columnDefinition = "longblob")
	private byte[] empImg;

	@OneToMany(mappedBy = "emps", fetch = FetchType.EAGER)
	private Set<EmpPermVO> empPermVO = new HashSet<>();

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "employee")
	@OrderBy("serviceReportId")
	private Set<ServiceReportVO> serviceReports;

	@Column(name = "RESET_TOKEN")
	private String resetToken;
	
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
	@Column(name = "TOKEN_EXPIRY")
	private LocalDateTime tokenExpiry;

	public EmployeeVO() {
		super();
	}

	public EmployeeVO(Integer employeeId, String empAccount, String empPassword, String empName, Integer roleTitle,
			Integer empStatus, Timestamp createdAt, Timestamp updatedAt, LocalDateTime lastLoginAt, byte[] empImg,
			Set<EmpPermVO> empPermVO) {
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

	public Integer getRoleTitle() {
		return roleTitle;
	}

	public void setRoleTitle(Integer roleTitle) {
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

	public LocalDateTime getLastLoginAt() {
		return lastLoginAt;
	}

	public void setLastLoginAt(LocalDateTime lastLoginAt) {
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
	

	public String getResetToken() {
		return resetToken;
	}

	public void setResetToken(String resetToken) {
		this.resetToken = resetToken;
	}

	public LocalDateTime getTokenExpiry() {
		return tokenExpiry;
	}

	public void setTokenExpiry(LocalDateTime tokenExpiry) {
		this.tokenExpiry = tokenExpiry;
	}



	public interface ValidGroup {
		interface First {
		}

		interface Second {
		}
	}

}
