package com.webond.service.dao;

import java.util.List;

import com.webond.service.model.ServiceTypeVO;

public interface ServiceTypeDAO_interface {
	
	public void insert(ServiceTypeVO svsType);
	public void delete(Integer PK);
	public void update(ServiceTypeVO svsType);
	
	public ServiceTypeVO findByPK (Integer PK);
	public List<ServiceTypeVO> getAll();
}
