package com.webond.activity.model;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.webond.activity.repository.ActivityRepository;

import jakarta.persistence.criteria.Predicate;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ActivityService {

	@Autowired
	private ActivityRepository repository;

	public List<ActivityVO> getAll() {
		return repository.findAll();
	}

	public List<ActivityVO> searchActivities(String keyword, Integer typeId, Byte status, Boolean onlyAvailable,
			String sort) {

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

	public List<ActivityVO> getActivitiesByMemberId(Integer memberId) {
		return repository.findByMemberId(memberId);
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

		if (activityVO.getRegistrationDeadline() == null) {
			return true;
		}

		return java.time.LocalDateTime.now().isBefore(activityVO.getRegistrationDeadline());
	}
}
