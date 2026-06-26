package com.webond.employee.model;

import java.util.Set;

import com.webond.service.model.ServiceReportVO;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

@Entity
@Table(name = "EMPLOYEE")
public class EmployeeVO implements java.io.Serializable  {

	private static final long serialVersionUID = 1L;

	   	@Id
	    @Column(name = "EMPLOYEE_ID")
	    private Integer employeeId;
	   	

		@OneToMany(cascade = CascadeType.ALL, mappedBy = "employee")
		@OrderBy("serviceReportId")
		private Set<ServiceReportVO> serviceReports;

}
