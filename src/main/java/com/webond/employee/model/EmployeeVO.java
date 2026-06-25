package com.webond.employee.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "EMPLOYEE")
public class EmployeeVO {

	@Id
	@Column(name = "EMPLOYEE_ID")
	private Integer empId;

	public EmployeeVO() {
		super();
	}

	public EmployeeVO(Integer empId) {
		super();
		this.empId = empId;
	}

	public Integer getEmpId() {
		return empId;
	}

	public void setEmpId(Integer empId) {
		this.empId = empId;
	}

	@Override
	public String toString() {
		return "EmployeeVO [empId=" + empId + "]";
	}
	
	

}
