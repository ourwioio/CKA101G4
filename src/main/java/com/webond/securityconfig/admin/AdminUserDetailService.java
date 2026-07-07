package com.webond.securityconfig.admin;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.webond.employee.model.EmployeeVO;
import com.webond.employee.repository.EmployeeRepository;


@Service
public class AdminUserDetailService implements UserDetailsService{

	private final EmployeeRepository empRepository;
	
	public AdminUserDetailService(EmployeeRepository empRepository) {
		this.empRepository = empRepository;
	}
	
	@Override
    @Transactional(readOnly = true) 
	public UserDetails loadUserByUsername(String empAccount) throws UsernameNotFoundException {
		
		EmployeeVO empVO = empRepository.findByEmpAccount(empAccount)
				.orElseThrow(()-> new UsernameNotFoundException("員工帳號["+ empAccount +"] 不存在"));
		

		List<GrantedAuthority> authorities = empVO.getEmpPermVO().stream()
				.map(empPerm -> new SimpleGrantedAuthority(empPerm.getPerms().getPermName()))
				.collect(Collectors.toList());
		
		return new AdminUserDetails(empVO, authorities);
		
	}
	
}
