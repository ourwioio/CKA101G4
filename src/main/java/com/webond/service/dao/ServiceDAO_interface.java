package com.webond.service.dao;

import java.util.List;

import com.webond.service.model.ServiceVO;

public interface ServiceDAO_interface {
	
	public void insert(ServiceVO svc);
	public void delete(Integer PK);
	public void update(ServiceVO svc);
	
	public ServiceVO getOne(Integer PK);
	public List<ServiceVO> getAll();
	
	//
	List<ServiceVO> getByServiceTypeId(Integer serviceTypeId);
}
