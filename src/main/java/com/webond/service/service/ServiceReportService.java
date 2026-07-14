package com.webond.service.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.webond.service.model.ServiceReportVO;
import com.webond.service.model.ServiceVO;
import com.webond.service.repository.ServiceReportRepository;
import com.webond.service.repository.ServiceRepository;

@Service
public class ServiceReportService {
	
	@Autowired
	ServiceReportRepository repository;

    @Autowired
    private ServiceRepository serviceRepository;

	
	//後台
	public List<ServiceReportVO> getAll(){
		return repository.findAll();
	}
	
	public ServiceReportVO getOneServiceReport(Integer servicetId) {
		Optional<ServiceReportVO> optional = repository.findById(servicetId);
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
		
		if (newStatus == null || newStatus == 0) {
			throw new IllegalStateException("尚未選擇審核結果，無法送出處理");
		}
		
		// 只覆蓋這頁允許編輯的欄位，其餘（serviceOrder、reporterMember、serviceReportTime）維持原值
		existing.setServiceReportStatus(newStatus);
		existing.setServiceReportHandleTime(LocalDateTime.now()); // 伺服器當下時間，不接受前端傳入
		existing.setEmployee(serviceReportVO.getEmployee());
		repository.save(existing);

	    if (newStatus == 1) {
	        ServiceVO serviceVO = existing.getService();
	        if (serviceVO != null) {
	            serviceVO.setStatus((byte) 3);
	            serviceRepository.save(serviceVO);
	        }
	    }
	}
	
	//前台
	public void checkCanReport(Integer serviceId) {
       ServiceVO serviceVO = serviceRepository.findById(serviceId).orElse(null);

       if(serviceVO == null) {
           throw new IllegalArgumentException("服務不存在，無法送出檢舉");    	   
       }
       
    }

   @Transactional
   public void submitReport(ServiceReportVO serviceReportVO, Integer serviceId) {

       ServiceVO serviceVO = serviceRepository.findById(serviceId).orElse(null);
       
       if(serviceVO == null) {
           throw new IllegalArgumentException("服務不存在，無法送出檢舉");    	   
       }

       serviceReportVO.setService(serviceVO);
       repository.save(serviceReportVO);
   }

}
