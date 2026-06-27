package com.webond.service.model;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.webond.service.repository.ServiceRepository;
import com.webond.service.repository.ServiceTypeRepository;

@Service
@Transactional
public class ServiceService {

    // 取代原本的 ServiceDAO_interface
    private final ServiceRepository serviceRepository;

    // 因為 ServiceVO 有 ManyToOne 關聯 ServiceTypeVO
    // 新增 / 修改時，要用 serviceTypeId 找到對應的 ServiceTypeVO
    private final ServiceTypeRepository serviceTypeRepository;

    public ServiceService(ServiceRepository serviceRepository,
                          ServiceTypeRepository serviceTypeRepository) {
        this.serviceRepository = serviceRepository;
        this.serviceTypeRepository = serviceTypeRepository;
    }

    // 新增服務
    public ServiceVO add(Integer serviceTypeId,
                         Integer memberId,
                         String serviceName,
                         String description,
                         Integer hourlyRate,
                         Byte status,
                         LocalDateTime createdAt) {

        validateService(serviceTypeId, serviceName, hourlyRate, status);

        ServiceVO svc = new ServiceVO();

        /*
         * JPA 關聯寫法：
         * 不要只 setServiceTypeId。
         * 因為真正負責 SERVICE_TYPE_ID 外鍵的是 serviceType 這個 @ManyToOne 欄位。
         */
        ServiceTypeVO serviceType = serviceTypeRepository.getReferenceById(serviceTypeId);
        svc.setServiceType(serviceType);

        svc.setMemberId(memberId);
        svc.setServiceName(serviceName.trim());
        svc.setDescription(description);
        svc.setHourlyRate(hourlyRate);
        svc.setStatus(status);

        /*
         * 如果 ServiceVO.createdAt 有 @CreationTimestamp，
         * 這行可以之後拿掉，讓 Hibernate 自動產生建立時間。
         */
        svc.setCreatedAt(createdAt);

        // 原本 dao.insert(svc)
        // 改成 repository.save(svc)
        return serviceRepository.save(svc);
    }

    // 修改服務
    public ServiceVO update(Integer serviceId,
                            Integer serviceTypeId,
                            Integer memberId,
                            String serviceName,
                            String description,
                            Integer hourlyRate,
                            Byte status) {

        validateService(serviceTypeId, serviceName, hourlyRate, status);
        
        
        ServiceVO svc = serviceRepository.findById(serviceId).orElse(null);

        if (svc == null) {
            return null;
        }

        ServiceTypeVO serviceType = serviceTypeRepository.getReferenceById(serviceTypeId);
        svc.setServiceType(serviceType);

        svc.setMemberId(memberId);
        svc.setServiceName(serviceName.trim());
        svc.setDescription(description);
        svc.setHourlyRate(hourlyRate);
        svc.setStatus(status);

        // 有 PK 且資料存在時，save() 就是更新
        return serviceRepository.save(svc);
    }

    // 刪除服務
    public void delete(Integer serviceId) {
        if (serviceRepository.existsById(serviceId)) {
            serviceRepository.deleteById(serviceId);
        }
    }

    // 查單一服務
    @Transactional(readOnly = true)
    public ServiceVO getOneService(Integer serviceId) {
        return serviceRepository.findOneWithServiceType(serviceId);
    }

    // 查全部服務
    @Transactional(readOnly = true)
    public List<ServiceVO> getAll() {
        return serviceRepository.findAllWithServiceType();
    }

    // 依服務類型查服務
    @Transactional(readOnly = true)
    public List<ServiceVO> getServicesByServiceTypeId(Integer serviceTypeId) {
        return serviceRepository.findByServiceTypeId(serviceTypeId);
    }

    // 共用驗證
    private void validateService(Integer serviceTypeId,
                                 String serviceName,
                                 Integer hourlyRate,
                                 Byte status) {

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
    }
}