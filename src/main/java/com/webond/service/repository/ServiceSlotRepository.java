package com.webond.service.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.webond.service.model.ServiceSlotVO;

public interface ServiceSlotRepository extends JpaRepository<ServiceSlotVO, Integer> {

	// 後台：查全部時段，順便載入服務
	@Query("""
			select ss from ServiceSlotVO ss
			left join fetch ss.service
			order by ss.serviceSlotId
			""")
	List<ServiceSlotVO> findAllWithService();

	// 後台：查單一時段，順便載入服務
	@Query("""
			select ss from ServiceSlotVO ss
			left join fetch ss.service
			where ss.serviceSlotId = :serviceSlotId
			""")
	ServiceSlotVO findOneWithService(Integer serviceSlotId);

	// 前台 / 後台：查某服務底下所有時段
	@Query("""
			select ss from ServiceSlotVO ss
			left join fetch ss.service
			where ss.service.serviceId = :serviceId
			order by ss.startTime
			""")
	List<ServiceSlotVO> findByServiceIdWithService(Integer serviceId);

	// 會員時段管理：查某服務底下所有時段
	List<ServiceSlotVO> findByService_ServiceIdOrderByStartTimeAsc(Integer serviceId);
	
	// 會員時段管理：查某服務底下「賣家看得到」的時段
	// 排除已封存 slotStatus = 3 的時段
	// 因為封存代表賣家已經刪除，只是為了保留訂單關聯才留在資料庫
	@Query("""
		       select ss from ServiceSlotVO ss
		       where ss.service.serviceId = :serviceId
		       and ss.slotStatus in :visibleStatusList
		       and ss.endTime > :now
		       order by ss.startTime
		       """)
	List<ServiceSlotVO> findVisibleSlotsByServiceId(Integer serviceId,
            List<Byte> visibleStatusList,
            LocalDateTime now);

	// 刪除某個服務底下的所有時段
	// 之前 delete service 前會用到
	void deleteByService_ServiceId(Integer serviceId);

	// 檢查新增時段是否跟既有時段重疊
	@Query("""
			select count(ss) > 0
			from ServiceSlotVO ss
			where ss.service.serviceId = :serviceId
			and ss.startTime < :endTime
			and ss.endTime > :startTime
			""")
	boolean existsOverlappingSlot(Integer serviceId, LocalDateTime startTime, LocalDateTime endTime);

	// 查某服務底下的某一筆時段
	// 用於刪除時段前確認這個 slot 是不是屬於這個 service
	@Query("""
			select ss from ServiceSlotVO ss
			left join fetch ss.service
			where ss.serviceSlotId = :serviceSlotId
			and ss.service.serviceId = :serviceId
			""")
	ServiceSlotVO findByServiceSlotIdAndServiceId(Integer serviceSlotId, Integer serviceId);

	// 服務時段有被訂單引用
	@Query("""
			select count(so) > 0
			from ServiceOrderVO so
			where so.serviceSlotId = :serviceSlotId
			""")
	boolean existsOrderByServiceSlotId(Integer serviceSlotId);

	// 將「可預約但已被訂單引用」的時段改成封存
	// 適合用在清空多筆時段時
	// 因為這些時段已經被 SERVICE_ORDER 外鍵引用，不能直接 delete
	@Modifying
	@Query("""
			update ServiceSlotVO ss
			set ss.slotStatus = :archivedStatus
			where ss.service.serviceId = :serviceId
			and ss.slotStatus = :availableStatus
			and exists (
			    select 1
			    from ServiceOrderVO so
			    where so.serviceSlotId = ss.serviceSlotId
			)
			""")
	int archiveAvailableSlotsWithOrders(Integer serviceId, Byte availableStatus, Byte archivedStatus);
	
	// 刪除「可預約且沒有被任何訂單引用」的時段
	// 適合用在清空多筆時段時
	// 沒有訂單引用的時段可以真的從 SERVICE_SLOT 刪除
	@Modifying
	@Query("""
	       delete from ServiceSlotVO ss
	       where ss.service.serviceId = :serviceId
	       and ss.slotStatus = :availableStatus
	       and not exists (
	           select 1
	           from ServiceOrderVO so
	           where so.serviceSlotId = ss.serviceSlotId
	       )
	       """)
	int deleteAvailableSlotsWithoutOrders(Integer serviceId,
	                                      Byte availableStatus);
	
	// 前台服務詳情：只顯示尚未開始、且未封存的服務時段
	@Query("""
	        select ss
	        from ServiceSlotVO ss
	        left join fetch ss.service
	        where ss.service.serviceId = :serviceId
	        and ss.slotStatus in (0, 1, 2)
	        and ss.startTime > :now
	        order by ss.startTime
	        """)
	List<ServiceSlotVO> findPublicFutureSlotsByServiceId(
	        Integer serviceId,
	        LocalDateTime now
	);
}