package com.webond.member.service;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.webond.activity.model.ActivityService;
import com.webond.member.model.MemberVO;
import com.webond.service.model.ServiceVO;
import com.webond.service.service.ServiceService;
import com.webond.venue.service.VenueService;

@ExtendWith(MockitoExtension.class)
class MemberDeactivationCoordinatorTest {

	@Mock
	private ActivityService activityService;

	@Mock
	private VenueService venueService;

	@Mock
	private ServiceService serviceService;

	@InjectMocks
	private MemberDeactivationCoordinator coordinator;

	@Test
	void handleDisabledMember_shouldNotifyAllModulesAndDeactivateOnlyActiveServices() {
		MemberVO member = member(9, 3);
		ServiceVO activeService = service(101, 1);
		ServiceVO inactiveService = service(102, 0);
		when(serviceService.getManageableServicesByMemberId(9))
				.thenReturn(List.of(activeService, inactiveService));

		coordinator.handleDisabledMember(member);

		verify(activityService).cancelActivitiesAndOrdersByDisabledMember(9);
		verify(venueService).removeVenue(member);
		verify(serviceService).deactivateBySeller(101, 9);
		verify(serviceService, never()).deactivateBySeller(102, 9);
	}

	@Test
	void handleDisabledMember_shouldIgnoreNormalMember() {
		coordinator.handleDisabledMember(member(9, 1));

		verifyNoInteractions(activityService, venueService, serviceService);
	}

	private MemberVO member(Integer memberId, int accountStatus) {
		MemberVO memberVO = new MemberVO();
		memberVO.setMemberId(memberId);
		memberVO.setAccountStatus((byte) accountStatus);
		return memberVO;
	}

	private ServiceVO service(Integer serviceId, int status) {
		ServiceVO serviceVO = new ServiceVO();
		serviceVO.setServiceId(serviceId);
		serviceVO.setStatus((byte) status);
		return serviceVO;
	}
}
