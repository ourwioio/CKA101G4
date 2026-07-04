package com.webond.activity.model;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.webond.activity.repository.ActivityRepository;

import java.util.List;
import java.util.Optional;

@Service
public class ActivityService {

	@Autowired
	private ActivityRepository repository;

	public List<ActivityVO> getAll() {
		return repository.findAll();
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