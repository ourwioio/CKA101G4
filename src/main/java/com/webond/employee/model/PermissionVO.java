package com.webond.employee.model;

import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;

import com.webond.employee.model.PermissionVO.ValidGroup;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.GroupSequence;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@GroupSequence({ValidGroup.First.class, ValidGroup.Second.class, PermissionVO.class})
@Entity
@Table(name = "permission")
public class PermissionVO {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "PERMISSION_ID", updatable = false)
	private Integer permId;		
	
	@NotBlank(message = "權限名稱請勿空白", groups = ValidGroup.First.class)
	@Pattern(regexp = "^[\\u4e00-\\u9fa5]{1,50}$", message = "權限名稱只能輸入中文，且長度不能超過 50 個字", groups = ValidGroup.Second.class)
	@Column(name = "PERMISSION_NAME")
	private String permName;	
	
	@NotBlank(message = "權限內容請勿空白", groups = ValidGroup.First.class)
	@Size(max = 255, message = "權限內容長度不能超過 255 個字", groups = ValidGroup.Second.class)
	@Column(name = "PERM_DESCRIPTION")
	private String permDsc;     
	
	@Column(name = "PERM_UPDATE_AT")
	private Timestamp permUpdatedAt;
	
	@OneToMany(mappedBy = "perms")
	private Set<EmpPermVO> empPermVO = new HashSet<>();
	
	public PermissionVO() {
		super();
	}

	public PermissionVO(Integer permId, String permName, String permDsc, Timestamp permUpdatedAt) {
		super();
		this.permId = permId;
		this.permName = permName;
		this.permDsc = permDsc;
		this.permUpdatedAt = permUpdatedAt;
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

	public Timestamp getPermUpdatedAt() {
		return permUpdatedAt;
	}

	public void setPermUpdatedAt(Timestamp permUpdatedAt) {
		this.permUpdatedAt = permUpdatedAt;
	}
	
	
	
	public interface ValidGroup {
	    interface First {}  
	    interface Second {}
	}
	

}
