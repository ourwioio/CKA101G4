package com.webond.servicereport.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.webond.servicereport.model.ServiceReportVO;

public interface ServiceReportRepository  extends JpaRepository<ServiceReportVO, Integer> {

}
