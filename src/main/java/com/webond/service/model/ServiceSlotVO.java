package com.webond.service.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "SERVICE_SLOT")
public class ServiceSlotVO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "SERVICE_SLOT_ID")
    private Integer serviceSlotId;

    // 實際存進資料庫的外鍵欄位
    @Column(name = "SERVICE_ID")
    private Integer serviceId;

    // 關聯查詢用：可以透過 service 拿到 serviceName、description 等資料
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SERVICE_ID", insertable = false, updatable = false)
    private ServiceVO service;

    @Column(name = "START_TIME")
    private LocalDateTime startTime;

    @Column(name = "END_TIME")
    private LocalDateTime endTime;

    @Column(name = "SLOT_STATUS")
    private Byte slotStatus;

    @Column(name = "LOCK_EXPIRES_AT")
    private LocalDateTime lockExpiresAt;

    public ServiceSlotVO() {
        super();
    }

    public Integer getServiceSlotId() {
        return serviceSlotId;
    }

    public void setServiceSlotId(Integer serviceSlotId) {
        this.serviceSlotId = serviceSlotId;
    }

    public Integer getServiceId() {
        return serviceId;
    }

    public void setServiceId(Integer serviceId) {
        this.serviceId = serviceId;
    }

    public ServiceVO getService() {
        return service;
    }

    public void setService(ServiceVO service) {
        this.service = service;

        if (service != null) {
            this.serviceId = service.getServiceId();
        } else {
            this.serviceId = null;
        }
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public Byte getSlotStatus() {
        return slotStatus;
    }

    public void setSlotStatus(Byte slotStatus) {
        this.slotStatus = slotStatus;
    }

    public LocalDateTime getLockExpiresAt() {
        return lockExpiresAt;
    }

    public void setLockExpiresAt(LocalDateTime lockExpiresAt) {
        this.lockExpiresAt = lockExpiresAt;
    }
}