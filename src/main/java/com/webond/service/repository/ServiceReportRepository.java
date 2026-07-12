package com.webond.service.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.webond.service.model.ServiceReportVO;

public interface ServiceReportRepository  extends JpaRepository<ServiceReportVO, Integer> {

    // 檢查這筆訂單是否已經被檢舉過
    boolean existsByServiceOrder_ServiceOrderId(Integer serviceOrderId);

}
