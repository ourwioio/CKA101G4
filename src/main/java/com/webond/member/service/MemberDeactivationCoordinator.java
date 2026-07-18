package com.webond.member.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.webond.activity.model.ActivityService;
import com.webond.member.model.MemberVO;
import com.webond.service.model.ServiceVO;
import com.webond.service.service.ServiceService;
import com.webond.venue.service.VenueService;

/**
 * 會員停權／註銷後的跨模組協調入口。
 *
 * <p>這個類別只負責「通知各模組處理」，不把活動、場地或服務的商業規則
 * 寫進會員模組。各模組實際能處理到的範圍，仍由該模組既有公開方法決定。</p>
 */
@Service
public class MemberDeactivationCoordinator {

	private static final Byte ACCOUNT_STATUS_DEACTIVATED = 2;
	private static final Byte ACCOUNT_STATUS_SUSPENDED = 3;
	private static final Byte SERVICE_STATUS_ACTIVE = 1;

	private final ActivityService activityService;
	private final VenueService venueService;
	private final ServiceService serviceService;

	public MemberDeactivationCoordinator(ActivityService activityService,
			VenueService venueService, ServiceService serviceService) {
		this.activityService = activityService;
		this.venueService = venueService;
		this.serviceService = serviceService;
	}

	/**
	 * 會員狀態變成 2（註銷）或 3（停權）後呼叫。
	 * 恢復成正常狀態時不會反向恢復已取消或已下架的資料。
	 */
	@Transactional
	public void handleDisabledMember(MemberVO memberVO) {
		if (memberVO == null || memberVO.getMemberId() == null || !isDisabledStatus(memberVO.getAccountStatus())) {
			return;
		}

		Integer memberId = memberVO.getMemberId();

		// 活動模組已有完整批次流程：取消未結束活動、相關訂單、退款標記與人數同步。
		activityService.cancelActivitiesAndOrdersByDisabledMember(memberId);

		// 場地模組目前的既有方法只負責將該會員上架中的場地下架。
		// 場地訂單、退款及時段釋放仍由場地模組日後補強，不在此跨模組協調器重寫。
		venueService.removeVenue(memberVO);

		// 服務模組目前只有單筆下架方法，因此在此取得該會員可管理的服務後逐筆呼叫。
		// 服務訂單與退款仍以服務模組既有／後續提供的公開方法為準。
		for (ServiceVO serviceVO : serviceService.getManageableServicesByMemberId(memberId)) {
			if (SERVICE_STATUS_ACTIVE.equals(serviceVO.getStatus())) {
				serviceService.deactivateBySeller(serviceVO.getServiceId(), memberId);
			}
		}
	}

	private boolean isDisabledStatus(Byte accountStatus) {
		return ACCOUNT_STATUS_DEACTIVATED.equals(accountStatus)
				|| ACCOUNT_STATUS_SUSPENDED.equals(accountStatus);
	}
}
