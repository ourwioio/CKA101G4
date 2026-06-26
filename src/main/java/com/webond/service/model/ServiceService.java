package com.webond.service.model;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.webond.service.dao.ServiceDAO_interface;

@Service
@Transactional
public class ServiceService {

	private ServiceDAO_interface dao;

//	public ServiceService() {
//		dao = new ServiceDAO();
//		dao = new ServiceDAOHibernate();
//	} 
//	

	public ServiceService(ServiceDAO_interface dao) {
		this.dao = dao;
	}

	public ServiceVO add(Integer serviceTypeId, Integer memberId, String serviceName, String description,
			Integer hourlyRate, Byte status, LocalDateTime createdAt) {

		if (serviceTypeId == null) {
			throw new IllegalArgumentException("服務類型不可為空");
		}

		if (serviceName == null || serviceName.trim().isEmpty()) {
			throw new IllegalArgumentException("服務名稱不可為空");
		}

		if (hourlyRate == null || hourlyRate < 0) {
			throw new IllegalArgumentException("每小時費率不可小於 0");
		}

		if (status == null || (status != 0 && status != 1)) {
			throw new IllegalArgumentException("服務狀態不合法");
		}

		ServiceVO svc = new ServiceVO();

		svc.setServiceTypeId(serviceTypeId);
		svc.setMemberId(memberId);
		svc.setServiceName(serviceName);
		svc.setDescription(description);
		svc.setHourlyRate(hourlyRate);
		svc.setStatus(status);
		svc.setCreatedAt(createdAt);

		dao.insert(svc);

		return svc;
	}

	public ServiceVO update(Integer serviceId, Integer serviceTypeId, Integer memberId, String serviceName,
			String description, Integer hourlyRate, Byte status) {

		ServiceVO svc = new ServiceVO();

		svc.setServiceId(serviceId);
		svc.setServiceTypeId(serviceTypeId);
		svc.setMemberId(memberId);
		svc.setServiceName(serviceName);
		svc.setDescription(description);
		svc.setHourlyRate(hourlyRate);
		svc.setStatus(status);

		dao.update(svc);

		return svc;
	}

	public void delete(Integer serviceId) {
		dao.delete(serviceId);
	}

	public ServiceVO getOneService(Integer serviceId) {
		return dao.getOne(serviceId);
	}

	public List<ServiceVO> getAll() {
		return dao.getAll();
	}

	// 關聯查詢

	public List<ServiceVO> getServicesByServiceTypeId(Integer serviceTypeId) {
		return dao.getByServiceTypeId(serviceTypeId);
	}
}