package com.webond.service.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
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

    // 只讀 FK 欄位，方便 DTO / 畫面顯示 serviceId
    @Column(name = "SERVICE_ID", insertable = false, updatable = false)
    private Integer serviceId;

    // 真正負責 SERVICE_ID 外鍵關聯
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SERVICE_ID")
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
    }

    public Integer getServiceSlotId() {
        return serviceSlotId;
    }

    public void setServiceSlotId(Integer serviceSlotId) {
        this.serviceSlotId = serviceSlotId;
    }

    public Integer getServiceId() {
        if (service != null) {
            return service.getServiceId();
        }
        return serviceId;
    }

    // 保留給表單 / DTO 綁定用
    // 但不要在這裡 new ServiceVO
    public void setServiceId(Integer serviceId) {
        this.serviceId = serviceId;
    }

    public ServiceVO getService() {
        return service;
    }

    public void setService(ServiceVO service) {
        this.service = service;
        this.serviceId = service != null ? service.getServiceId() : null;
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