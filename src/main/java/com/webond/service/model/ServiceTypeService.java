package com.webond.service.model;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.webond.service.dao.ServiceTypeDAO_interface;

@Service
@Transactional
public class ServiceTypeService {
	private ServiceTypeDAO_interface dao;

//	public ServiceTypeService() {
////		dao = new ServiceTypeDAO();
//		dao = new ServiceTypeDAOHibernate();
//	}
	public ServiceTypeService(ServiceTypeDAO_interface dao) {
		this.dao = dao;
	}

	@Transactional(readOnly = true)
	public ServiceTypeVO findByPK(Integer PK) {
		return dao.findByPK(PK);
	}

	@Transactional(readOnly = true)
	public List<ServiceTypeVO> getAll() {
		return dao.getAll();
	}

	public void delete(Integer PK) {
		dao.delete(PK);
	}

	public ServiceTypeVO add(String name, String descrip, Integer mode, String URL) {
		ServiceTypeVO svcTVO = new ServiceTypeVO();
		svcTVO.setTypeName(name);
		svcTVO.setDescrip(descrip);
		svcTVO.setTypeMode(mode);
		svcTVO.setImgURL(URL);
		dao.insert(svcTVO);
		return svcTVO;
	}

	public ServiceTypeVO update(Integer PK, String name, String descrip, Integer mode, String URL) {
		ServiceTypeVO svcTVO = new ServiceTypeVO();
		svcTVO.setSvcTypeID(PK);
		svcTVO.setTypeName(name);
		svcTVO.setDescrip(descrip);
		svcTVO.setTypeMode(mode);
		svcTVO.setImgURL(URL);
		dao.update(svcTVO);
		return svcTVO;
	}
}
