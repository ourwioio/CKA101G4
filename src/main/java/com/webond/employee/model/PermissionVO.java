package com.webond.employee.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
	
	public PermissionVO() {
		super();
	}
	
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
	
	

}
