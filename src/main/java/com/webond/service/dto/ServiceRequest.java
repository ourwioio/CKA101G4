package com.webond.service.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class ServiceRequest {

    @NotNull(message = "請選擇服務類型")
    @Min(value = 1, message = "請選擇服務類型")
    private Integer serviceTypeId;

    @NotBlank(message = "服務名稱請勿空白")
    @Size(max = 50, message = "服務名稱不可超過 50 個字")
    private String serviceName;
    
	@NotBlank(message = "服務描述請勿空白")
    private String description;

    @NotNull(message = "每小時費率必須是整數")
    @Min(value = 0, message = "每小時費率不可小於 0")
    private Integer hourlyRate;
    
    @NotBlank(message = "請選擇服務縣市")
    private String serviceCity;

    @NotBlank(message = "請選擇服務行政區")
    private String serviceDistrict;

    @Size(max = 255, message = "服務地點說明不可超過 255 個字")
    private String serviceLocation;
    
    public Integer getServiceTypeId() {
		return serviceTypeId;
	}

	public void setServiceTypeId(Integer serviceTypeId) {
		this.serviceTypeId = serviceTypeId;
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

	public String getServiceCity() {
		return serviceCity;
	}

	public void setServiceCity(String serviceCity) {
		this.serviceCity = serviceCity;
	}

	public String getServiceDistrict() {
		return serviceDistrict;
	}

	public void setServiceDistrict(String serviceDistrict) {
		this.serviceDistrict = serviceDistrict;
	}

	public String getServiceLocation() {
		return serviceLocation;
	}

	public void setServiceLocation(String serviceLocation) {
		this.serviceLocation = serviceLocation;
	}
	
	
}