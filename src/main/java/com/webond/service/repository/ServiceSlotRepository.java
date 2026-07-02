package com.webond.service.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.webond.service.model.ServiceSlotVO;

public interface ServiceSlotRepository extends JpaRepository<ServiceSlotVO, Integer> {

    // 查全部時段，順便把 service 查出來，避免 LAZY 問題
    @Query("""
           select ss from ServiceSlotVO ss
           left join fetch ss.service
           order by ss.serviceSlotId
           """)
    List<ServiceSlotVO> findAllWithService();

    // 查單一時段，順便把 service 查出來
    @Query("""
           select ss from ServiceSlotVO ss
           left join fetch ss.service
           where ss.serviceSlotId = :serviceSlotId
           """)
    ServiceSlotVO findOneWithService(Integer serviceSlotId);

    // 依服務 ID 查時段，順便把 service 查出來
    @Query("""
           select ss from ServiceSlotVO ss
           left join fetch ss.service
           where ss.service.serviceId = :serviceId
           order by ss.startTime
           """)
    List<ServiceSlotVO> findByServiceIdWithService(Integer serviceId);
    
    @Modifying
    @Query("""
           delete from ServiceSlotVO ss
           where ss.service.serviceId = :serviceId
           """)
    void deleteByServiceId(Integer serviceId);
}