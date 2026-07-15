package com.webond.service.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.webond.member.model.MemberReportVO;
import com.webond.member.model.MemberVO;
import com.webond.member.model.NotificationVO;
import com.webond.member.repository.MemberReportRepository;
import com.webond.member.service.MemberService;
import com.webond.member.service.NotificationService;
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
    
    @Autowired
    private MemberReportRepository memberReportRepository;

    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private MemberService memberService;

	
	//後台
	public List<ServiceReportVO> getAll(){
		return repository.findAll();
	}
	
	public List<ServiceReportVO> getByStatus(Byte status) {
	    return repository.findByserviceReportStatus(status);
	}
	
	public ServiceReportVO getOneServiceReport(Integer servicetId) {
		Optional<ServiceReportVO> optional = repository.findById(servicetId);
		return optional.orElse(null);
	}
	
	public void updateServiceReport(ServiceReportVO serviceReportVO) {
		ServiceReportVO existing = repository.findById(serviceReportVO.getServiceReportId())
				.orElseThrow(() -> new IllegalArgumentException("找不到檢舉編號：" + serviceReportVO.getServiceReportId()));
		
		MemberVO memberVO = memberService.getOneMember(existing.getService().getMemberId());
		MemberVO reporterId = existing.getReporterMember();
		

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
		
		//發通知
		if (newStatus == 1) {

            NotificationVO notification = new NotificationVO();
            notification.setMember(memberVO);
            notification.setTitle("【服務下架通知】您的服務已被檢舉涉嫌違規");
            notification.setContent("日前我們收到針對您服務的檢舉，經初步審查，服務因違反平台規範已暫時下架。");
            notification.setNotificationType((byte) 3);   // 這裡要填正確的類型代碼
            notificationService.addNotification(notification);
            
            ServiceVO serviceVO = existing.getService();
	        if (serviceVO != null) {
	            serviceVO.setStatus((byte) 3);
	            serviceRepository.save(serviceVO);
	        }
	        
	        if (memberVO != null) {
	            MemberReportVO memberReportVO = new MemberReportVO();
	            memberReportVO.setReporter(reporterId);  // 服務檢舉的發起人
	            memberReportVO.setReported(memberVO); // 服務檢舉的發起人
	            memberReportVO.setReportCategory(3);;                         // 被記點的會員（服務擁有者）
	            memberReportVO.setReportContent("因服務檢舉審核通過，服務已違規下架，自動記點");
	            memberReportVO.setViolationPoints(1);
	            memberReportVO.setReportStatus(1); // 狀態代碼要跟組員確認
	            memberReportRepository.save(memberReportVO);
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
