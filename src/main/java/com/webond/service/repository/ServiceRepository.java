package com.webond.service.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.webond.service.model.ServiceVO;

public interface ServiceRepository extends JpaRepository<ServiceVO, Integer> {

    // 查全部服務，順便把 serviceType 一起查出來
    @Query("""
           select s from ServiceVO s
           left join fetch s.serviceType
           order by s.serviceId
           """)
    List<ServiceVO> findAllWithServiceType();

    // 依服務類型查服務，順便把 serviceType 一起查出來
    @Query("""
           select s from ServiceVO s
           join fetch s.serviceType st
           where st.svcTypeID = :serviceTypeId
           """)
    List<ServiceVO> findByServiceTypeId(Integer serviceTypeId);
    
    @Query("""
    	       select s from ServiceVO s
    	       left join fetch s.serviceType
    	       where s.serviceId = :serviceId
    	       """)
    	ServiceVO findOneWithServiceType(Integer serviceId);
}
