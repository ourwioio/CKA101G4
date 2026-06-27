package com.webond.service.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.webond.service.model.ServiceVO;

public interface ServiceRepository extends JpaRepository<ServiceVO, Integer> {

    // 查全部服務，並用 fetch join 一次把 serviceType 關聯資料查出來
    // 避免 Thymeleaf 顯示 service.serviceType.typeName 時發生 LazyInitializationException
    @Query("""
           select s from ServiceVO s
           left join fetch s.serviceType
           order by s.serviceId
           """)
    List<ServiceVO> findAllWithServiceType();

    // 依服務類型 ID 查詢服務，並順便把 serviceType 關聯資料查出來
    // serviceTypeId 是從 ServiceTypeVO.svcTypeID 來比對
    @Query("""
           select s from ServiceVO s
           join fetch s.serviceType st
           where st.svcTypeID = :serviceTypeId
           order by s.serviceId
           """)
    List<ServiceVO> findByServiceTypeId(Integer serviceTypeId);

    // 查單一服務，並順便把 serviceType 關聯資料查出來
    // 用在 detail 頁面或需要顯示服務類型名稱的地方
    @Query("""
           select s from ServiceVO s
           left join fetch s.serviceType
           where s.serviceId = :serviceId
           """)
    ServiceVO findOneWithServiceType(Integer serviceId);
}