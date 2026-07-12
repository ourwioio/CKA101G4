package com.webond.service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ServiceReportDTO {
	
	 @NotBlank(message = "請輸入檢舉內容")
	    @Size(max = 500, message = "檢舉內容不可超過500字")
	    private String serviceReportCom;

	 public String getServiceReportCom() {
		 return serviceReportCom;
	 }

	 public void setServiceReportCom(String serviceReportCom) {
		 this.serviceReportCom = serviceReportCom;
	 }

}
