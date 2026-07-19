package com.webond.activity.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.webond.activity.repository.ActivityRepository;

@ExtendWith(MockitoExtension.class)
class ActivityServiceTest {

	@Mock
	private ActivityRepository activityRepository;

	@Mock
	private ActivityOrderService orderService;

	@InjectMocks
	private ActivityService activityService;

	@Test
	void cancelActivitiesAndOrdersByDisabledMember_shouldCancelHostedActivitiesAndSyncAffectedCounts() {
		ActivityVO hostedActivity = activity(1, 9);
		ActivityVO alreadyCancelledActivity = activity(2, 5);
		alreadyCancelledActivity.setActivityStatus((byte) 2);
		ActivityVO joinedActivity = activity(3, 6);

		when(activityRepository.findByMemberIdAndEndTimeAfter(eq(9), any(LocalDateTime.class)))
				.thenReturn(List.of(hostedActivity, alreadyCancelledActivity));
		when(orderService.cancelOrdersByDisabledBuyer(eq(9), any(LocalDateTime.class), anyString()))
				.thenReturn(Set.of(3));
		when(activityRepository.findById(1)).thenReturn(Optional.of(hostedActivity));
		when(activityRepository.findById(2)).thenReturn(Optional.of(alreadyCancelledActivity));
		when(activityRepository.findById(3)).thenReturn(Optional.of(joinedActivity));
		when(orderService.getActiveBookingCount(1)).thenReturn(0);
		when(orderService.getActiveBookingCount(2)).thenReturn(0);
		when(orderService.getActiveBookingCount(3)).thenReturn(4);

		activityService.cancelActivitiesAndOrdersByDisabledMember(9);

		assertEquals((byte) 2, hostedActivity.getActivityStatus());
		assertEquals((byte) 2, alreadyCancelledActivity.getActivityStatus());
		assertEquals(0, hostedActivity.getAttendeesCount());
		assertEquals(0, alreadyCancelledActivity.getAttendeesCount());
		assertEquals(4, joinedActivity.getAttendeesCount());
		verify(orderService).cancelOrdersByActivity(eq(1), anyString());
		verify(orderService).cancelOrdersByActivity(eq(2), anyString());
		verify(activityRepository).saveAll(List.of(hostedActivity, alreadyCancelledActivity));
	}

	@Test
	void cancelActivityByHost_shouldMarkActivityCancelledAndCancelOrders() {
		ActivityVO activityVO = activity(8, 3);
		activityVO.setMemberId(9);
		activityVO.setEndTime(LocalDateTime.now().plusDays(1));
		when(activityRepository.findById(8)).thenReturn(Optional.of(activityVO));
		when(orderService.getActiveBookingCount(8)).thenReturn(0);

		boolean cancelled = activityService.cancelActivityByHost(8, 9);

		assertEquals(true, cancelled);
		assertEquals((byte) 2, activityVO.getActivityStatus());
		assertEquals(0, activityVO.getAttendeesCount());
		verify(activityRepository, times(2)).save(activityVO);
		verify(orderService).cancelOrdersByActivity(eq(8), anyString());
	}

	private ActivityVO activity(Integer activityId, Integer attendeesCount) {
		ActivityVO activityVO = new ActivityVO();
		activityVO.setActivityId(activityId);
		activityVO.setActivityStatus((byte) 0);
		activityVO.setAttendeesCount(attendeesCount);
		return activityVO;
	}
}
