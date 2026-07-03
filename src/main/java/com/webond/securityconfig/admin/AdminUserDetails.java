package com.webond.securityconfig.admin;

import java.util.Collection;
import java.util.Collections;

import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.webond.employee.model.EmployeeVO;

public class AdminUserDetails implements UserDetails{
	
	private final EmployeeVO empVO;
	private final Collection<? extends GrantedAuthority> authorities;
	
	public AdminUserDetails(EmployeeVO empVO, Collection<? extends GrantedAuthority> authorities) {
		this.empVO = empVO;
		this.authorities = authorities;
	}
	
	public EmployeeVO getEmpVO() {
		return empVO;
	}
	
	public Integer getEmpId() {
		return empVO.getEmployeeId();
	}


	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return this.authorities != null ? this.authorities : Collections.emptyList();
	}


	@Override
    public String getPassword() {
		return empVO.getEmpPassword();
    }


	@Override
	public String getUsername() {
		return empVO.getEmpAccount();
	}


	@Override
	public boolean isAccountNonExpired() {
		return true;
	}


	@Override
	public boolean isAccountNonLocked() {
		return true;
	}


	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}


	@Override
	public boolean isEnabled() {	
		return empVO.getEmpStatus() == 1;
	}
	
}
