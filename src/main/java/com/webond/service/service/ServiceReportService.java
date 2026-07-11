package com.webond.service.service;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.webond.service.model.ServiceReportVO;
import com.webond.service.repository.ServiceReportRepository;

@Service
public class ServiceReportService {
	
	@Autowired
	ServiceReportRepository repository;
	
	//後台
	public List<ServiceReportVO> getAll(){
		return repository.findAll();
	}
	
	public ServiceReportVO getOneServiceReport(Integer serviceReportId) {
		Optional<ServiceReportVO> optional = repository.findById(serviceReportId);
		return optional.orElse(null);
	}
	
	public void updateServiceReport(ServiceReportVO serviceReportVO) {
		ServiceReportVO existing = repository.findById(serviceReportVO.getServiceReportId())
				.orElseThrow(() -> new IllegalArgumentException("找不到檢舉編號：" + serviceReportVO.getServiceReportId()));

		Byte currentStatus = existing.getServiceReportStatus();
		Byte newStatus = serviceReportVO.getServiceReportStatus();

		// 已經審核過（1 或 2）就不能改回未審核（0）
		if (currentStatus != null && currentStatus != 0 && newStatus != null && newStatus == 0) {
			throw new IllegalStateException("此檢舉已審核過，不能改回「未審核」狀態");
		}
		
		// 只覆蓋這頁允許編輯的欄位，其餘（serviceOrder、reporterMember、serviceReportTime）維持原值
		existing.setServiceReportStatus(newStatus);
		existing.setServiceReportHandleTime(new Timestamp(System.currentTimeMillis())); // 伺服器當下時間，不接受前端傳入
		existing.setEmployee(serviceReportVO.getEmployee());
		repository.save(existing);
	}

}
