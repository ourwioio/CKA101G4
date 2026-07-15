package com.webond.service.service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.webond.service.model.ServiceSlotVO;
import com.webond.service.model.ServiceVO;
import com.webond.service.repository.ServiceRepository;
import com.webond.service.repository.ServiceSlotRepository;

@Service
@Transactional
public class ServiceSlotService {

	// 最遠只能新增今天起 14 天內的時段
	private static final int MAX_BOOKING_DAYS = 14;

	// 一次最多新增 12 小時
	private static final int MAX_BLOCK_HOURS = 12;

	// slot 狀態
	private static final byte SLOT_AVAILABLE = 0; // 可預約
	private static final byte SLOT_LOCKED = 1; // 暫時鎖定
	private static final byte SLOT_BOOKED = 2; // 已預約
	private static final byte SLOT_ARCHIVED = 3; // 已刪除後封存

	private final ServiceSlotRepository serviceSlotRepository;
	private final ServiceRepository serviceRepository;
	private final SlotStatusWebSocketService slotStatusWebSocketService;

	public ServiceSlotService(ServiceSlotRepository serviceSlotRepository, ServiceRepository serviceRepository,
			SlotStatusWebSocketService slotStatusWebSocketService) {
		this.serviceSlotRepository = serviceSlotRepository;
		this.serviceRepository = serviceRepository;
		this.slotStatusWebSocketService = slotStatusWebSocketService;
	}

	// =====================
	// 會員時段管理
	// =====================

	// 查某會員自己的某個服務底下所有時段
	@Transactional(readOnly = true)
	public List<ServiceSlotVO> getSlotsBySeller(Integer serviceId, Integer loginMemberId) {

		// 先確認這個服務是不是這個會員的
		getOwnServiceOrThrow(serviceId, loginMemberId);

		// 查詢賣家看得到的時段，排除已封存 slotStatus = 3
		return serviceSlotRepository.findVisibleSlotsByServiceId(serviceId,
				List.of(SLOT_AVAILABLE, SLOT_LOCKED, SLOT_BOOKED), LocalDateTime.now());
	}

	// 會員新增一批時段
	//
	// splitMinutes:
	// 0 = 不切，整段上架
	// 30 = 每 30 分鐘切一段
	// 60 = 每 60 分鐘切一段
	// 120 = 每 120 分鐘切一段
	// 240 = 每 240 分鐘切一段
	//
	// endDayOffset:
	// 0 = 同日
	// 1 = 隔日
	public void addSlotsBySeller(Integer serviceId, Integer loginMemberId, LocalDate slotDate, LocalTime startTime,
			LocalTime endTime, Integer endDayOffset, Integer splitMinutes) {

		// 先確認這個 service 是不是目前登入會員自己的服務
		ServiceVO serviceVO = getOwnServiceOrThrow(serviceId, loginMemberId);

		if (slotDate == null) {
			throw new IllegalArgumentException("請選擇日期");
		}

		if (startTime == null) {
			throw new IllegalArgumentException("請選擇開始時間");
		}

		if (endTime == null) {
			throw new IllegalArgumentException("請選擇結束時間");
		}

		if (endDayOffset == null) {
			endDayOffset = 0;
		}

		LocalDateTime startDateTime = LocalDateTime.of(slotDate, startTime);
		LocalDateTime endDateTime = LocalDateTime.of(slotDate.plusDays(endDayOffset), endTime);

		validateSlotInput(startDateTime, endDateTime, splitMinutes);

		// 不切，直接存一筆大時段
		if (splitMinutes == 0) {
			saveOneSlot(serviceVO, serviceId, startDateTime, endDateTime);
			return;
		}

		// 有切分，依 splitMinutes 切成多筆時段
		LocalDateTime currentStart = startDateTime;

		while (currentStart.isBefore(endDateTime)) {

			LocalDateTime currentEnd = currentStart.plusMinutes(splitMinutes);

			// 理論上 validate 已經確保可以整除
			// 這裡只是保險
			if (currentEnd.isAfter(endDateTime)) {
				break;
			}

			saveOneSlot(serviceVO, serviceId, currentStart, currentEnd);

			currentStart = currentEnd;
		}
	}

	// 會員刪除自己的某個服務時段
	// 會員刪除自己的某個服務時段
	public void deleteSlotBySeller(Integer serviceId,
	                               Integer serviceSlotId,
	                               Integer loginMemberId) {

	    // 先確認 service 是自己的
	    getOwnServiceOrThrow(serviceId, loginMemberId);

	    // 再確認 slot 屬於這個 service
	    ServiceSlotVO slotVO = serviceSlotRepository.findByServiceSlotIdAndServiceId(
	            serviceSlotId,
	            serviceId
	    );

	    if (slotVO == null) {
	        throw new IllegalArgumentException("查無此時段，或此時段不屬於你的服務");
	    }

	    // 只有可預約的時段可以刪除
	    if (!Byte.valueOf(SLOT_AVAILABLE).equals(slotVO.getSlotStatus())) {
	        throw new IllegalArgumentException("只有可預約的時段可以刪除");
	    }

	    boolean hasOrder = serviceSlotRepository.existsOrderByServiceSlotId(serviceSlotId);

	    // 沒有訂單引用，真的刪除
	    if (!hasOrder) {
	        serviceSlotRepository.delete(slotVO);

	        // 推送給前台：這筆時段消失
	        slotStatusWebSocketService.publishSlotStatus(
	                serviceId,
	                serviceSlotId,
	                SLOT_ARCHIVED
	        );

	        return;
	    }

	    // 有訂單引用，不能真刪，改成封存
	    slotVO.setSlotStatus(SLOT_ARCHIVED);
	    serviceSlotRepository.save(slotVO);

	    // 推送給前台：這筆時段變封存，前端會隱藏
	    slotStatusWebSocketService.publishSlotStatus(
	            serviceId,
	            serviceSlotId,
	            SLOT_ARCHIVED
	    );
	}

	// =====================
	// 後台 / 原本 CRUD
	// =====================

	// 新增服務時段
	public ServiceSlotVO add(Integer serviceId, LocalDateTime startTime, LocalDateTime endTime, Byte slotStatus,
			LocalDateTime lockExpiresAt) {

		validateSlot(serviceId, startTime, endTime, slotStatus);

		ServiceSlotVO svcS = new ServiceSlotVO();

		ServiceVO service = serviceRepository.getReferenceById(serviceId);
		svcS.setService(service);

		svcS.setStartTime(startTime);
		svcS.setEndTime(endTime);
		svcS.setSlotStatus(slotStatus);
		svcS.setLockExpiresAt(lockExpiresAt);

		return serviceSlotRepository.save(svcS);
	}

	// 修改服務時段
	public ServiceSlotVO update(Integer serviceSlotId, Integer serviceId, LocalDateTime startTime,
			LocalDateTime endTime, Byte slotStatus, LocalDateTime lockExpiresAt) {

		validateSlot(serviceId, startTime, endTime, slotStatus);

		ServiceSlotVO svcS = serviceSlotRepository.findById(serviceSlotId).orElse(null);

		if (svcS == null) {
			return null;
		}

		ServiceVO service = serviceRepository.getReferenceById(serviceId);
		svcS.setService(service);

		svcS.setStartTime(startTime);
		svcS.setEndTime(endTime);
		svcS.setSlotStatus(slotStatus);
		svcS.setLockExpiresAt(lockExpiresAt);

		return serviceSlotRepository.save(svcS);
	}

	// 刪除服務時段
	public void delete(Integer serviceSlotId) {
		if (serviceSlotRepository.existsById(serviceSlotId)) {
			serviceSlotRepository.deleteById(serviceSlotId);
		}
	}

	// 查單一服務時段
	@Transactional(readOnly = true)
	public ServiceSlotVO getOneServiceSlot(Integer serviceSlotId) {
		return serviceSlotRepository.findOneWithService(serviceSlotId);
	}

	// 查全部服務時段
	@Transactional(readOnly = true)
	public List<ServiceSlotVO> getAll() {
		return serviceSlotRepository.findAllWithService();
	}

	// 查某個服務底下的所有時段
	@Transactional(readOnly = true)
	public List<ServiceSlotVO> getByServiceId(Integer serviceId) {
		return serviceSlotRepository.findByServiceIdWithService(serviceId);
	}
	
	// 前台公開服務詳情：只查尚未開始的時段
	@Transactional(readOnly = true)
	public List<ServiceSlotVO> getPublicFutureSlotsByServiceId(
	        Integer serviceId) {

	    return serviceSlotRepository
	            .findPublicFutureSlotsByServiceId(
	                    serviceId,
	                    LocalDateTime.now()
	            );
	}

	
	// 會員清空自己的某個服務底下全部可預約時段
	// 有訂單引用的時段：改成封存 slotStatus = 3
	// 沒有訂單引用的時段：真的刪除
	public int deleteAvailableSlotsBySeller(Integer serviceId,
	                                        Integer loginMemberId) {

	    // 先確認這個服務是不是自己的
	    getOwnServiceOrThrow(serviceId, loginMemberId);

	    // 可預約 + 有訂單引用的時段：不能真的刪，改成封存
	    int archivedCount = serviceSlotRepository.archiveAvailableSlotsWithOrders(
	            serviceId,
	            SLOT_AVAILABLE,
	            SLOT_ARCHIVED
	    );

	    // 可預約 + 沒有訂單引用的時段：可以真的刪除
	    int deletedCount = serviceSlotRepository.deleteAvailableSlotsWithoutOrders(
	            serviceId,
	            SLOT_AVAILABLE
	    );

	    return archivedCount + deletedCount;
	}
	// =====================
	// 會員時段輸入驗證
	// =====================

	private void validateSlotInput(LocalDateTime startDateTime, LocalDateTime endDateTime, Integer splitMinutes) {

		if (startDateTime == null) {
			throw new IllegalArgumentException("開始時間不可為空");
		}

		if (endDateTime == null) {
			throw new IllegalArgumentException("結束時間不可為空");
		}

		if (!endDateTime.isAfter(startDateTime)) {
			throw new IllegalArgumentException("結束時間必須晚於開始時間");
		}

		LocalDateTime now = LocalDateTime.now();

		if (startDateTime.isBefore(now)) {
			throw new IllegalArgumentException("不能新增過去的時段");
		}

		if (startDateTime.toLocalDate().isAfter(now.toLocalDate().plusDays(MAX_BOOKING_DAYS))) {
			throw new IllegalArgumentException("只能新增今天起兩週內的服務時段");
		}

		long minutes = Duration.between(startDateTime, endDateTime).toMinutes();

		if (minutes <= 0) {
			throw new IllegalArgumentException("時段長度不合法");
		}

		if (minutes > MAX_BLOCK_HOURS * 60) {
			throw new IllegalArgumentException("一次最多只能新增 12 小時的時段");
		}

		validateSplitMinutes(splitMinutes);

		if (splitMinutes != 0 && minutes % splitMinutes != 0) {
			throw new IllegalArgumentException("總時長必須可以被切分方式整除");
		}
	}

	// 驗證切分方式
	private void validateSplitMinutes(Integer splitMinutes) {

		if (splitMinutes == null) {
			throw new IllegalArgumentException("請選擇切分方式");
		}

		if (splitMinutes != 0 && splitMinutes != 30 && splitMinutes != 60 && splitMinutes != 120
				&& splitMinutes != 240) {
			throw new IllegalArgumentException("切分方式不合法");
		}
	}

	// 實際存一筆時段
	private void saveOneSlot(ServiceVO serviceVO, Integer serviceId, LocalDateTime startDateTime,
			LocalDateTime endDateTime) {

		boolean overlapped = serviceSlotRepository.existsOverlappingSlot(serviceId, startDateTime, endDateTime);

		if (overlapped) {
			throw new IllegalArgumentException(
					"時段重疊：" + formatDateTime(startDateTime) + " ~ " + formatDateTime(endDateTime));
		}

		ServiceSlotVO slotVO = new ServiceSlotVO();

		slotVO.setService(serviceVO);
		slotVO.setStartTime(startDateTime);
		slotVO.setEndTime(endDateTime);
		slotVO.setSlotStatus(SLOT_AVAILABLE);
		slotVO.setLockExpiresAt(null);

		serviceSlotRepository.save(slotVO);
	}

	// =====================
	// 原本後台時段基本驗證
	// =====================

	private void validateSlot(Integer serviceId, LocalDateTime startTime, LocalDateTime endTime, Byte slotStatus) {

		if (serviceId == null || serviceId <= 0) {
			throw new IllegalArgumentException("服務編號不可為空");
		}

		if (startTime == null) {
			throw new IllegalArgumentException("開始時間不可為空");
		}

		if (endTime == null) {
			throw new IllegalArgumentException("結束時間不可為空");
		}

		if (!endTime.isAfter(startTime)) {
			throw new IllegalArgumentException("結束時間必須晚於開始時間");
		}

		if (slotStatus == null || slotStatus < 0 || slotStatus > 3) {
		    throw new IllegalArgumentException("時段狀態不合法");
		}
	}

	// =====================
	// 共用：確認服務是不是自己的
	// =====================

	private ServiceVO getOwnServiceOrThrow(Integer serviceId, Integer loginMemberId) {

		if (loginMemberId == null) {
			throw new IllegalArgumentException("請先登入");
		}

		return serviceRepository.findByServiceIdAndMemberId(serviceId, loginMemberId)
				.orElseThrow(() -> new IllegalArgumentException("查無此服務，或你沒有權限操作"));
	}

	// 簡單格式化錯誤訊息用
	private String formatDateTime(LocalDateTime dateTime) {
		return dateTime.toString().replace("T", " ");
	}
}