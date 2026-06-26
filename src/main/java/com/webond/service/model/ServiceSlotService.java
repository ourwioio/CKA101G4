package com.webond.service.model;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.webond.service.dao.ServiceSlotDAO_interface;

@Service
@Transactional
public class ServiceSlotService {

    private final ServiceSlotDAO_interface dao;

    public ServiceSlotService(ServiceSlotDAO_interface dao) {
        this.dao = dao;
    }

    // 新增服務時段
    public ServiceSlotVO add(
            Integer serviceId,
            LocalDateTime startTime,
            LocalDateTime endTime,
            Byte slotStatus,
            LocalDateTime lockExpiresAt) {

        ServiceSlotVO svcS = new ServiceSlotVO();

        svcS.setServiceId(serviceId);
        svcS.setStartTime(startTime);
        svcS.setEndTime(endTime);
        svcS.setSlotStatus(slotStatus);
        svcS.setLockExpiresAt(lockExpiresAt);

        dao.insert(svcS);

        return svcS;
    }

    // 修改服務時段
    public ServiceSlotVO update(
            Integer serviceSlotId,
            Integer serviceId,
            LocalDateTime startTime,
            LocalDateTime endTime,
            Byte slotStatus,
            LocalDateTime lockExpiresAt) {

        ServiceSlotVO svcS = new ServiceSlotVO();

        svcS.setServiceSlotId(serviceSlotId);
        svcS.setServiceId(serviceId);
        svcS.setStartTime(startTime);
        svcS.setEndTime(endTime);
        svcS.setSlotStatus(slotStatus);
        svcS.setLockExpiresAt(lockExpiresAt);

        dao.update(svcS);

        return svcS;
    }

    // 刪除服務時段
    public void delete(Integer serviceSlotId) {
        dao.delete(serviceSlotId);
    }

    // 查單一服務時段
    @Transactional(readOnly = true)
    public ServiceSlotVO getOneServiceSlot(Integer serviceSlotId) {
        return dao.getOne(serviceSlotId);
    }

    // 查全部服務時段
    @Transactional(readOnly = true)
    public List<ServiceSlotVO> getAll() {
        return dao.getAll();
    }

    // 查某個服務底下的所有時段
    @Transactional(readOnly = true)
    public List<ServiceSlotVO> getByServiceId(Integer serviceId) {
        return dao.getByServiceId(serviceId);
    }
}