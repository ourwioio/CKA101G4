package com.webond.employee.model;

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

@Entity
@Table(name = "permission")
public class PermissionVO {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "PERMISSION_ID", updatable = false)
	private Integer permId;		
	
	@Column(name = "PERMISSION_NAME")
	private String permName;	
	
	@Column(name = "PERM_DESCRIPTION")
	private String permDsc;     
	
	@OneToMany(mappedBy = "perms", fetch = FetchType.EAGER)
	private Set<EmpPermVO> empPermVO = new HashSet<>();
	
	public PermissionVO() {
		super();
	}
	
	// 沒有OneToMany
	public PermissionVO(Integer permId, String permName, String permDsc) {
		super();
		this.permId = permId;
		this.permName = permName;
		this.permDsc = permDsc;
	}

	public Integer getPermId() {
		return permId;
	}
	public void setPermId(Integer permId) {
		this.permId = permId;
	}
	public String getPermName() {
		return permName;
	}
	public void setPermName(String permName) {
		this.permName = permName;
	}
	public String getPermDsc() {
		return permDsc;
	}
	public void setPermDsc(String permDsc) {
		this.permDsc = permDsc;
	}

	public Set<EmpPermVO> getEmpPermVO() {
		return empPermVO;
	}

	public void setEmpPermVO(Set<EmpPermVO> empPermVO) {
		this.empPermVO = empPermVO;
	}
	
	
	
	
	

}
