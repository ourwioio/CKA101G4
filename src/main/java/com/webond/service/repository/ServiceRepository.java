package com.webond.service.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.webond.service.model.ServiceVO;

public interface ServiceRepository extends JpaRepository<ServiceVO, Integer> {

    // =========================
    // 前台公開查詢
    // =========================

    // 前台公開查詢：查所有已上架服務，並一起載入服務類型
    @Query("""
           select s from ServiceVO s
           left join fetch s.serviceType
           where s.status = 1
           order by s.serviceId
           """)
    List<ServiceVO> findActiveServices();

    // 前台公開查詢：依服務類型查已上架服務
    @Query("""
           select s from ServiceVO s
           join fetch s.serviceType st
           where st.svcTypeID = :serviceTypeId
           and s.status = 1
           order by s.serviceId
           """)
    List<ServiceVO> findActiveServicesByServiceTypeId(Integer serviceTypeId);

    // 前台公開查詢：依服務 ID 查已上架服務詳情
    @Query("""
           select s from ServiceVO s
           left join fetch s.serviceType
           where s.serviceId = :serviceId
           and s.status = 1
           """)
    ServiceVO findActiveServiceById(Integer serviceId);

 // 前台公開查詢：依名稱、描述、類型或地區搜尋已上架服務
    @Query("""
           select distinct s
           from ServiceVO s
           left join fetch s.serviceType st
           where s.status = 1
           and (
                s.serviceName like concat('%', :keyword, '%')
                or s.description like concat('%', :keyword, '%')
                or st.typeName like concat('%', :keyword, '%')
                or s.serviceCity like concat('%', :keyword, '%')
                or s.serviceDistrict like concat('%', :keyword, '%')
                or s.serviceLocation like concat('%', :keyword, '%')
           )
           order by s.serviceId
           """)
    List<ServiceVO> searchActiveServices(String keyword);
    // =========================
    // 會員中心查詢
    // =========================

    // 會員中心：查登入會員自己的可管理服務
    // 只顯示 0 下架、1 上架
    @Query("""
           select s from ServiceVO s
           left join fetch s.serviceType
           where s.memberId = :memberId
           and s.status in (0, 1)
           order by s.serviceId desc
           """)
    List<ServiceVO> findManageableServicesByMemberId(Integer memberId);

    // 會員中心：查某會員自己的某一筆服務
    // 用在修改、下架、重新上架、刪除前做權限檢查
    @Query("""
           select s from ServiceVO s
           left join fetch s.serviceType
           where s.serviceId = :serviceId
           and s.memberId = :memberId
           """)
    Optional<ServiceVO> findByServiceIdAndMemberId(Integer serviceId, Integer memberId);

    // =========================
    // 後台之後再做
    // =========================

    @Query("""
           select s from ServiceVO s
           left join fetch s.serviceType
           order by s.serviceId
           """)
    List<ServiceVO> findAllWithServiceType();

    @Query("""
           select s from ServiceVO s
           join fetch s.serviceType st
           where st.svcTypeID = :serviceTypeId
           order by s.serviceId
           """)
    List<ServiceVO> findByServiceTypeId(Integer serviceTypeId);

    @Query("""
           select s from ServiceVO s
           left join fetch s.serviceType
           where s.serviceId = :serviceId
           """)
    ServiceVO findOneWithServiceType(Integer serviceId);
}