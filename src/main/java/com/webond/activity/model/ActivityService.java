package com.webond.activity.model;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.webond.activity.repository.ActivityRepository;

import jakarta.persistence.criteria.Predicate;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.time.LocalDateTime;

@Service
public class ActivityService {

	private static final boolean ENFORCE_REGISTRATION_TIME = false;
	private static final Byte ACTIVITY_STATUS_CANCELLED = 2;
	private static final String DISABLED_MEMBER_CANCELLATION_REASON = "會員停權或註銷，系統取消活動及訂單";
	private static final String HOST_CANCELLATION_REASON = "活動發起者下架活動，系統取消訂單";

	@Autowired
	private ActivityRepository repository;

	@Autowired
	private ActivityOrderService activityOrderSvc;

	public List<ActivityVO> getAll() {
		return repository.findAll();
	}

	public List<ActivityVO> searchActivities(String keyword, Integer typeId, Byte status, Boolean onlyAvailable,
			String sort) {
		return searchActivities(keyword, typeId, status, onlyAvailable, sort, null);
	}

	public List<ActivityVO> searchActivities(String keyword, Integer typeId, Byte status, Boolean onlyAvailable,
			String sort, Integer excludeMemberId) {

		Specification<ActivityVO> spec = (root, query, cb) -> {
			List<Predicate> predicates = new ArrayList<>();

			if (keyword != null && !keyword.trim().isEmpty()) {
				String likeKeyword = "%" + keyword.trim().toLowerCase() + "%";
				predicates.add(cb.or(cb.like(cb.lower(root.<String>get("activityTitle")), likeKeyword),
						cb.like(cb.lower(root.<String>get("activityDescription")), likeKeyword)));
			}

			if (typeId != null) {
				predicates.add(cb.equal(root.get("activityTypeId"), typeId));
			}

			if (status != null) {
				predicates.add(cb.equal(root.get("activityStatus"), status));
			} else {
				// 前台未指定狀態時，不公開顯示已下架／已取消的活動。
				predicates.add(cb.notEqual(root.get("activityStatus"), ACTIVITY_STATUS_CANCELLED));
			}

			if (excludeMemberId != null) {
				predicates.add(cb.notEqual(root.get("memberId"), excludeMemberId));
			}

			if (Boolean.TRUE.equals(onlyAvailable)) {
				predicates.add(cb.lessThan(cb.coalesce(root.<Integer>get("attendeesCount"), 0),
						root.<Integer>get("maxParticipants")));
			}

			return cb.and(predicates.toArray(new Predicate[0]));
		};

		return repository.findAll(spec, buildActivitySort(sort));
	}

	private Sort buildActivitySort(String sort) {
		if ("priceAsc".equals(sort)) {
			return Sort.by(Sort.Direction.ASC, "activityPrice");
		}
		if ("priceDesc".equals(sort)) {
			return Sort.by(Sort.Direction.DESC, "activityPrice");
		}
		if ("endTimeAsc".equals(sort)) {
			return Sort.by(Sort.Direction.ASC, "endTime");
		}
		return Sort.by(Sort.Direction.DESC, "createdAt");
	}

	public ActivityVO getOneActivity(Integer id) {
		Optional<ActivityVO> optional = repository.findById(id);
		return optional.orElse(null);
	}

	public ActivityVO saveActivity(ActivityVO vo) {
		return repository.save(vo);
	}

	public void deleteActivity(Integer id) {
		repository.deleteById(id);
	}

	@Transactional
	public void syncAttendeesFromOrders() {
		for (ActivityVO activityVO : repository.findAll()) {
			syncAttendeesFromOrders(activityVO.getActivityId());
		}
	}

	@Transactional
	public void syncAttendeesFromOrders(Integer activityId) {
		ActivityVO activityVO = getOneActivity(activityId);

		if (activityVO == null) {
			return;
		}

		Integer activeBookingCount = activityOrderSvc.getActiveBookingCount(activityId);
		activityVO.setAttendeesCount(activeBookingCount == null ? 0 : activeBookingCount);
		repository.save(activityVO);
	}

	public boolean isFull(ActivityVO activityVO) {
		if (activityVO == null || activityVO.getMaxParticipants() == null) {
			return false;
		}
		Integer attendeesCount = activityVO.getAttendeesCount() == null ? 0 : activityVO.getAttendeesCount();
		return attendeesCount >= activityVO.getMaxParticipants();
	}

	public boolean hasReachedMinimum(ActivityVO activityVO) {
		if (activityVO == null || activityVO.getMinParticipants() == null) {
			return false;
		}
		Integer attendeesCount = activityVO.getAttendeesCount() == null ? 0 : activityVO.getAttendeesCount();
		return attendeesCount >= activityVO.getMinParticipants();
	}

	public List<ActivityVO> getActivitiesByMemberId(Integer memberId) {
		return repository.findByMemberId(memberId);
	}

	/**
	 * Activity-module integration point for member suspension or account
	 * cancellation. Ended activities and completed orders are deliberately kept
	 * so that history and reviews remain available.
	 */
	@Transactional
	public void cancelActivitiesAndOrdersByDisabledMember(Integer memberId) {
		if (memberId == null) {
			return;
		}

		LocalDateTime now = LocalDateTime.now();
		Set<Integer> affectedActivityIds = new HashSet<>();
		List<ActivityVO> hostedActivities = repository.findByMemberIdAndEndTimeAfter(memberId, now);

		for (ActivityVO activityVO : hostedActivities) {
			Integer activityId = activityVO.getActivityId();
			activityVO.setActivityStatus(ACTIVITY_STATUS_CANCELLED);
			affectedActivityIds.add(activityId);
			activityOrderSvc.cancelOrdersByActivity(activityId, DISABLED_MEMBER_CANCELLATION_REASON);
		}
		repository.saveAll(hostedActivities);

		affectedActivityIds.addAll(activityOrderSvc.cancelOrdersByDisabledBuyer(
				memberId, now, DISABLED_MEMBER_CANCELLATION_REASON));

		for (Integer activityId : affectedActivityIds) {
			syncAttendeesFromOrders(activityId);
		}
	}

	/**
	 * 由活動發起者自行下架尚未結束的活動。
	 * 活動資料不會刪除，而是改為狀態 2（已取消），並同步取消未完成訂單與退款標記。
	 */
	@Transactional
	public boolean cancelActivityByHost(Integer activityId, Integer hostMemberId) {
		if (activityId == null || hostMemberId == null) {
			return false;
		}

		ActivityVO activityVO = repository.findById(activityId).orElse(null);
		if (activityVO == null || !hostMemberId.equals(activityVO.getMemberId())) {
			return false;
		}

		if (ACTIVITY_STATUS_CANCELLED.equals(activityVO.getActivityStatus())) {
			return true;
		}

		if (activityVO.getEndTime() == null || !activityVO.getEndTime().isAfter(LocalDateTime.now())) {
			return false;
		}

		activityVO.setActivityStatus(ACTIVITY_STATUS_CANCELLED);
		repository.save(activityVO);
		activityOrderSvc.cancelOrdersByActivity(activityId, HOST_CANCELLATION_REASON);
		syncAttendeesFromOrders(activityId);
		return true;
	}

	// 報名成功後增加已報名人數
	@Transactional
	public void increaseAttendees(Integer activityId, Integer bookingCount) {

		ActivityVO activityVO = getOneActivity(activityId);

		if (activityVO == null) {
			throw new RuntimeException("查無此活動");
		}

		if (bookingCount == null || bookingCount < 1) {
			bookingCount = 1;
		}

		Integer currentCount = activityVO.getAttendeesCount();

		if (currentCount == null) {
			currentCount = 0;
		}

		activityVO.setAttendeesCount(currentCount + bookingCount);

		repository.save(activityVO);
	}

	// 檢查是否還可以報名
	public boolean canRegister(Integer activityId, Integer bookingCount) {

		ActivityVO activityVO = getOneActivity(activityId);

		if (activityVO == null) {
			return false;
		}

		if (activityVO.getActivityStatus() == null || activityVO.getActivityStatus() != 0) {
			return false;
		}

		if (isActivityEnded(activityId)) {
			return false;
		}

		if (ENFORCE_REGISTRATION_TIME && !isRegistrationOpen(activityId)) {
			return false;
		}

		Integer current = activityVO.getAttendeesCount();
		Integer max = activityVO.getMaxParticipants();

		if (current == null) {
			current = 0;
		}

		if (bookingCount == null || bookingCount < 1) {
			bookingCount = 1;
		}

		return (current + bookingCount) <= max;
	}

	public boolean isRegistrationOpen(Integer activityId) {

		ActivityVO activityVO = getOneActivity(activityId);

		if (activityVO == null) {
			return false;
		}

		if (activityVO.getActivityStatus() == null || activityVO.getActivityStatus() != 0) {
			return false;
		}

		if (isActivityEnded(activityId)) {
			return false;
		}

		if (!ENFORCE_REGISTRATION_TIME) {
			return true;
		}

		LocalDateTime now = LocalDateTime.now();

		if (activityVO.getRegistrationStartTime() != null && now.isBefore(activityVO.getRegistrationStartTime())) {
			return false;
		}

		if (activityVO.getRegistrationDeadline() != null && now.isAfter(activityVO.getRegistrationDeadline())) {
			return false;
		}

		return true;
	}

	public boolean isActivityEnded(Integer activityId) {
		ActivityVO activityVO = getOneActivity(activityId);

		if (activityVO == null || activityVO.getEndTime() == null) {
			return false;
		}

		return LocalDateTime.now().isAfter(activityVO.getEndTime());
	}
}
