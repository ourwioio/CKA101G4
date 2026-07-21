package com.webond.service.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

	// =========================================================
	// 前台：複合條件搜尋已上架服務
	//
	// 不同條件之間使用 AND：
	// 1. 關鍵字：服務名稱或描述包含文字
	// 2. 服務類型：精確符合
	// 3. 縣市：精確符合
	// 4. 行政區：精確符合
	// 5. 費率：最低與最高金額區間
	// =========================================================

	@Query("""
			select distinct s
			from ServiceVO s
			left join fetch s.serviceType st
			where s.status = 1

			and (
			     :keyword = ''
			     or s.serviceName like concat('%', :keyword, '%')
			     or s.description like concat('%', :keyword, '%')
			)

			and (
			     :serviceTypeId is null
			     or st.svcTypeID = :serviceTypeId
			)

			and (
			     :serviceCity = ''
			     or s.serviceCity = :serviceCity
			)

			and (
			     :serviceDistrict = ''
			     or s.serviceDistrict = :serviceDistrict
			)

			and (
			     :minRate is null
			     or s.hourlyRate >= :minRate
			)

			and (
			     :maxRate is null
			     or s.hourlyRate <= :maxRate
			)

			order by s.serviceId
			""")
	List<ServiceVO> searchActiveServices(@Param("keyword") String keyword,

			@Param("serviceTypeId") Integer serviceTypeId,

			@Param("serviceCity") String serviceCity,

			@Param("serviceDistrict") String serviceDistrict,

			@Param("minRate") Integer minRate,

			@Param("maxRate") Integer maxRate);
	// =========================
	// 會員中心查詢
	// =========================

	// 會員中心：查登入會員自己的可管理服務
	// 只顯示 0 下架、1 上架
	@Query("""
			select s from ServiceVO s
			left join fetch s.serviceType
			where s.memberId = :memberId
			and s.status in (0, 1, 3)
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

	// 一次下架所有服務
	@Modifying
	@Query("""
			update ServiceVO s
			set s.status = 3
			where s.memberId = :memberId
			and s.status = 1
			""")
	int disableAllActiveServicesByMemberId(@Param("memberId") Integer memberId);
}