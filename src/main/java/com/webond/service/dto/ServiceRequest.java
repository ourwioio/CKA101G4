package com.webond.service.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class ServiceRequest {

    @NotNull(message = "請選擇服務類型")
    @Min(value = 1, message = "請選擇服務類型")
    private Integer serviceTypeId;

    @NotNull(message = "會員編號必須大於 0")
    @Min(value = 1, message = "會員編號必須大於 0")
    private Integer memberId;

    @NotBlank(message = "服務名稱請勿空白")
    @Size(max = 50, message = "服務名稱不可超過 50 個字")
    private String serviceName;

    @NotBlank(message = "服務描述請勿空白")
    private String description;

    @NotNull(message = "每小時費率必須是整數")
    @Min(value = 0, message = "每小時費率不可小於 0")
    private Integer hourlyRate;

    @NotNull(message = "請選擇服務狀態")
    private Integer status;

    public Integer getServiceTypeId() {
        return serviceTypeId;
    }

    public void setServiceTypeId(Integer serviceTypeId) {
        this.serviceTypeId = serviceTypeId;
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

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}