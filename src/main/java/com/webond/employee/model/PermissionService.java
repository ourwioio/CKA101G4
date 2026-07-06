package com.webond.employee.model;

import java.util.List;
import java.util.Optional;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.webond.employee.model.PermissionVO;
import com.webond.employee.repository.PermissionRepository;

@Service
public class PermissionService {
	
	@Autowired
	PermissionRepository repository;
	
	@Autowired
	private SessionFactory sessionFactory;
	
	public void addEmp(PermissionVO permVO) {
		repository.save(permVO);
	}
	
	public void updateEmp(PermissionVO permVO) {
		repository.save(permVO);
	}
	
	public void deleteEmp(Integer permId) {
		if(repository.existsById(permId))
			repository.deleteById(permId);
	}
	
	public PermissionVO getOneEmp(Integer permId) {
		Optional<PermissionVO> optional = repository.findById(permId);
		return optional.orElse(null);
	}
	
	public List<PermissionVO> getAll(){
		return repository.findAll();
	}
	
	

}
