package com.webond.service.model;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.webond.service.repository.ServiceRepository;
import com.webond.service.repository.ServiceSlotRepository;

@Service
@Transactional
public class ServiceSlotService {

    // 取代原本的 ServiceSlotDAO_interface
    private final ServiceSlotRepository serviceSlotRepository;

    // 因為 ServiceSlotVO 有 ManyToOne 關聯 ServiceVO
    // 所以新增 / 修改時，要透過 serviceId 找到 ServiceVO
    private final ServiceRepository serviceRepository;

    public ServiceSlotService(ServiceSlotRepository serviceSlotRepository,
                              ServiceRepository serviceRepository) {
        this.serviceSlotRepository = serviceSlotRepository;
        this.serviceRepository = serviceRepository;
    }

    // 新增服務時段
    public ServiceSlotVO add(
            Integer serviceId,
            LocalDateTime startTime,
            LocalDateTime endTime,
            Byte slotStatus,
            LocalDateTime lockExpiresAt) {

        validateSlot(serviceId, startTime, endTime, slotStatus);

        ServiceSlotVO svcS = new ServiceSlotVO();

        // 重點：
        // JPA 關聯寫法不是只 setServiceId
        // 而是拿到 ServiceVO 物件後，塞進 service 欄位
        ServiceVO service = serviceRepository.getReferenceById(serviceId);
        svcS.setService(service);

        svcS.setStartTime(startTime);
        svcS.setEndTime(endTime);
        svcS.setSlotStatus(slotStatus);
        svcS.setLockExpiresAt(lockExpiresAt);

        // 原本 dao.insert(svcS)
        // 改成 repository.save(svcS)
        return serviceSlotRepository.save(svcS);
    }

    // 修改服務時段
    public ServiceSlotVO update(
            Integer serviceSlotId,
            Integer serviceId,
            LocalDateTime startTime,
            LocalDateTime endTime,
            Byte slotStatus,
            LocalDateTime lockExpiresAt) {

        validateSlot(serviceId, startTime, endTime, slotStatus);

        // Spring Data JPA 建議先查原本資料，再修改欄位
        // 不建議直接 new 一個 VO 後 save，避免覆蓋掉其他欄位
        ServiceSlotVO svcS = serviceSlotRepository.findById(serviceSlotId).orElse(null);

        if (svcS == null) {
            return null;
        }

        ServiceVO service = serviceRepository.getReferenceById(serviceId);
        svcS.setService(service);

        svcS.setStartTime(startTime);
        svcS.setEndTime(endTime);
        svcS.setSlotStatus(slotStatus);
        svcS.setLockExpiresAt(lockExpiresAt);

        // 有 PK 且資料存在時，save() 就是更新
        return serviceSlotRepository.save(svcS);
    }

    // 刪除服務時段
    public void delete(Integer serviceSlotId) {
        if (serviceSlotRepository.existsById(serviceSlotId)) {
            serviceSlotRepository.deleteById(serviceSlotId);
        }
    }

    // 查單一服務時段
    @Transactional(readOnly = true)
    public ServiceSlotVO getOneServiceSlot(Integer serviceSlotId) {
        return serviceSlotRepository.findOneWithService(serviceSlotId);
    }

    // 查全部服務時段
    @Transactional(readOnly = true)
    public List<ServiceSlotVO> getAll() {
        return serviceSlotRepository.findAllWithService();
    }

    // 查某個服務底下的所有時段
    @Transactional(readOnly = true)
    public List<ServiceSlotVO> getByServiceId(Integer serviceId) {
        return serviceSlotRepository.findByServiceIdWithService(serviceId);
    }

    // 時段基本驗證
    private void validateSlot(Integer serviceId,
                              LocalDateTime startTime,
                              LocalDateTime endTime,
                              Byte slotStatus) {

        if (serviceId == null || serviceId <= 0) {
            throw new IllegalArgumentException("服務編號不可為空");
        }

        if (startTime == null) {
            throw new IllegalArgumentException("開始時間不可為空");
        }

        if (endTime == null) {
            throw new IllegalArgumentException("結束時間不可為空");
        }

        if (!endTime.isAfter(startTime)) {
            throw new IllegalArgumentException("結束時間必須晚於開始時間");
        }

        if (slotStatus == null || slotStatus < 0 || slotStatus > 2) {
            throw new IllegalArgumentException("時段狀態不合法");
        }
    }
}