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
	
	@Column(name = "SERVICE_TYPE_ID", insertable = false, updatable = false)
	private Integer serviceTypeId;
	
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
	
//	public Integer getServiceTypeId() {
//		return serviceTypeId;
//	}
//	public void setServiceTypeId(Integer serviceTypeId) {
//		this.serviceTypeId = serviceTypeId;
//	}
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
	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}
	
	public Integer getServiceTypeId() {
	    if (serviceType != null && serviceType.getSvcTypeID() != null) {
	        return serviceType.getSvcTypeID();
	    }
	    return serviceTypeId;
	}

	public void setServiceTypeId(Integer serviceTypeId) {
	    this.serviceTypeId = serviceTypeId;

	    if (serviceTypeId != null) {
	        ServiceTypeVO serviceTypeVO = new ServiceTypeVO();
	        serviceTypeVO.setSvcTypeID(serviceTypeId);
	        this.serviceType = serviceTypeVO;
	    } else {
	        this.serviceType = null;
	    }
	}

	public ServiceTypeVO getServiceType() {
	    return serviceType;
	}

	public void setServiceType(ServiceTypeVO serviceType) {
	    this.serviceType = serviceType;

	    if (serviceType != null) {
	        this.serviceTypeId = serviceType.getSvcTypeID();
	    } else {
	        this.serviceTypeId = null;
	    }
	}
	
	
}
