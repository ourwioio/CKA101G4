package com.webond.venue.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.webond.venue.model.VenueReviewVO;
import com.webond.venue.model.VenueVO;
import com.webond.venue.repository.VenueRepository;
import com.webond.venue.repository.VenueReviewRepository;

@Service
public class VenueReviewService {

	@Autowired
	VenueReviewRepository repository;

	@Autowired
	VenueRepository venueRepository; // 連動更新場地狀態

	// 狀態常數，避免 magic number 散落各處
	public static final byte STATUS_REVIEWING = 0; // 審核中
	public static final byte STATUS_APPROVED = 1; // 審核通過
	public static final byte STATUS_REJECTED = 2; // 審核未通過
	
	// 場地狀態常數（對照 VENUE 表的 venueStatus）
	private static final byte VENUE_STATUS_INACTIVE = 0; // 下架
	private static final byte VENUE_STATUS_ACTIVE = 1;   // 上架

	// ===== 新增 =====
	public void addVenueReview(VenueReviewVO venueReviewVO) {
		venueReviewVO.setVenueReviewId(null); // 確保走 INSERT
		venueReviewVO.setReviewStatus(STATUS_REVIEWING); // 新增一律預設審核中
		repository.save(venueReviewVO);
	}

	// ===== 修改 =====
	/**
	 * 修改審核紀錄：若該筆已經是「審核通過」，鎖定狀態與審核相關欄位， 不允許透過修改表單改回審核中或改成未通過（即使前端被繞過送出也一樣擋下）。
	 */
	@Transactional
	public void updateVenueReview(VenueReviewVO venueReviewVO) {
		VenueReviewVO existingVenueReview = getOneVenueReview(venueReviewVO.getVenueReviewId());
		if (existingVenueReview != null && existingVenueReview.getReviewStatus() == STATUS_APPROVED) {
			venueReviewVO.setReviewStatus(STATUS_APPROVED);
			venueReviewVO.setEmployeeId(existingVenueReview.getEmployeeId());
			venueReviewVO.setReviewNote(existingVenueReview.getReviewNote());
			venueReviewVO.setReviewedAt(existingVenueReview.getReviewedAt());
		}
		repository.save(venueReviewVO);
	}

	// ===== 刪除 =====
	public void deleteVenueReview(Integer venueReviewId) {
		if (repository.existsById(venueReviewId))
			repository.deleteById(venueReviewId);
	}

	// ===== 查詢 =====
	public VenueReviewVO getOneVenueReview(Integer venueReviewId) {
		Optional<VenueReviewVO> optional = repository.findById(venueReviewId);
		return optional.orElse(null);
	}

	public List<VenueReviewVO> getAll() {
		return repository.findAll();
	}

	public List<VenueReviewVO> getByStatus(Byte reviewStatus) {
		return repository.findByReviewStatus(reviewStatus);
	}

	public List<VenueReviewVO> getByVenueId(Integer venueId) {
		return repository.findByVenueId(venueId);
	}

	public List<VenueReviewVO> getByEmployeeId(Integer employeeId) {
		return repository.findByEmployeeId(employeeId);
	}

	// ===== 業務邏輯：審核通過 =====
    /**
     * 審核通過：更新審核紀錄狀態，並將對應場地的 venueStatus 切換為上架中。
     */
	@Transactional
	public void approve(Integer venueReviewId, Integer employeeId, String reviewNote) {
		VenueReviewVO venueReview = getOneVenueReview(venueReviewId);
		if (venueReview == null)
			return;
		venueReview.setReviewStatus(STATUS_APPROVED);
		venueReview.setEmployeeId(employeeId);
		venueReview.setReviewNote(reviewNote);
		venueReview.setReviewedAt(LocalDateTime.now());
		repository.save(venueReview);
		
        VenueVO venue = venueRepository.findById(venueReview.getVenueId()).orElse(null);
        if (venue != null) {
            venue.setVenueStatus(VENUE_STATUS_ACTIVE);
            venueRepository.save(venue);
        }
	}

	// ===== 業務邏輯：審核未通過 =====
    /**
     * 審核未通過：更新審核紀錄狀態，並將對應場地的 venueStatus 切換為下架。
     */
	@Transactional
	public void reject(Integer venueReviewId, Integer employeeId, String reviewNote) {
		VenueReviewVO venueReview = getOneVenueReview(venueReviewId);
		if (venueReview == null)
			return;
		venueReview.setReviewStatus(STATUS_REJECTED);
		venueReview.setEmployeeId(employeeId);
		venueReview.setReviewNote(reviewNote);
		venueReview.setReviewedAt(LocalDateTime.now());
		repository.save(venueReview);
		
        VenueVO venue = venueRepository.findById(venueReview.getVenueId()).orElse(null);
        if (venue != null) {
            venue.setVenueStatus(VENUE_STATUS_INACTIVE);
            venueRepository.save(venue);
        }
	}
}
