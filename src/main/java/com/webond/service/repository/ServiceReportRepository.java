package com.webond.service.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.webond.service.model.ServiceReportVO;

public interface ServiceReportRepository  extends JpaRepository<ServiceReportVO, Integer> {


}
