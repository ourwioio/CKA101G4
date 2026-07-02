package com.webond.service.model;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

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
@Table(name = "SERVICE")
public class ServiceVO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "SERVICE_ID")
    private Integer serviceId;

    // 只讀 FK 欄位，方便 DTO / 畫面顯示使用
    @Column(name = "SERVICE_TYPE_ID", insertable = false, updatable = false)
    private Integer serviceTypeId;

    // 真正負責關聯 SERVICE_TYPE_ID
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SERVICE_TYPE_ID")
    private ServiceTypeVO serviceType;

    @Column(name = "MEMBER_ID")
    private Integer memberId;

    @Column(name = "SERVICE_NAME")
    private String serviceName;

    @Column(name = "DESCRIPTION")
    private String description;

    @Column(name = "HOURLY_RATE")
    private Integer hourlyRate;

    @Column(name = "STATUS")
    private Byte status;

    @CreationTimestamp
    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Integer getServiceId() {
        return serviceId;
    }

    public void setServiceId(Integer serviceId) {
        this.serviceId = serviceId;
    }

    public Integer getServiceTypeId() {
        if (serviceType != null) {
            return serviceType.getSvcTypeID();
        }
        return serviceTypeId;
    }

    public void setServiceTypeId(Integer serviceTypeId) {
        this.serviceTypeId = serviceTypeId;
    }

    public ServiceTypeVO getServiceType() {
        return serviceType;
    }

    public void setServiceType(ServiceTypeVO serviceType) {
        this.serviceType = serviceType;
        this.serviceTypeId = serviceType != null ? serviceType.getSvcTypeID() : null;
    }

    public Integer getMemberId() {
        return memberId;
    }

    public void setMemberId(Integer memberId) {
        this.memberId = memberId;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getHourlyRate() {
        return hourlyRate;
    }

    public void setHourlyRate(Integer hourlyRate) {
        this.hourlyRate = hourlyRate;
    }

    public Byte getStatus() {
        return status;
    }

    public void setStatus(Byte status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    // 一般新增時不用手動 set，交給 @CreationTimestamp
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}