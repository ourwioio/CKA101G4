package com.webond.venue.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.webond.venue.model.VenueOrderVO;
import com.webond.venue.model.VenueReportVO;
import com.webond.venue.model.VenueVO;
import com.webond.venue.repository.VenueOrderRepository;
import com.webond.venue.repository.VenueReportRepository;
import com.webond.venue.repository.VenueRepository;

@Service
public class VenueReportService {

	@Autowired
	VenueReportRepository repository;

	@Autowired
	VenueRepository venueRepository; // 連動更新場地狀態

	@Autowired
	VenueOrderRepository venueOrderRepository; // 透過訂單找場地

	// 狀態常數，避免 magic number 散落各處
	public static final byte STATUS_REVIEWING = 0; // 審核中
	public static final byte STATUS_APPROVED = 1; // 通過（下架場地）
	public static final byte STATUS_REJECTED = 2; // 未通過

	// 場地狀態常數（對照 VENUE 表的 venueStatus）
	private static final byte VENUE_STATUS_INACTIVE = 0; // 下架
	private static final byte VENUE_STATUS_ACTIVE = 1; // 上架

	// ===== 新增 =====
	public void addVenueReport(VenueReportVO venueReportVO) {
		venueReportVO.setVenueReportId(null); // 確保走 INSERT
		venueReportVO.setReportStatus(STATUS_REVIEWING); // 新增一律預設審核中
		repository.save(venueReportVO);
	}

	// ===== 修改 =====
	/**
	 * 修改檢舉紀錄：若該筆已經處理完成（通過或未通過），鎖定處理狀態與處理相關欄位，
	 * 不允許透過修改表單改回審核中或變更處理結果（即使前端被繞過送出也一樣擋下）。
	 */
	@Transactional
	public void updateVenueReport(VenueReportVO venueReportVO) {
		VenueReportVO existingVenueReport = getOneVenueReport(venueReportVO.getVenueReportId());
		if (existingVenueReport != null && existingVenueReport.getReportStatus() != STATUS_REVIEWING) {
			venueReportVO.setReportStatus(existingVenueReport.getReportStatus());
			venueReportVO.setEmployeeId(existingVenueReport.getEmployeeId());
			venueReportVO.setHandledAt(existingVenueReport.getHandledAt());
		}
		repository.save(venueReportVO);
	}

	// ===== 刪除 =====
	public void deleteVenueReport(Integer venueReportId) {
		if (repository.existsById(venueReportId))
			repository.deleteById(venueReportId);
	}

	// ===== 查詢 =====
	public VenueReportVO getOneVenueReport(Integer venueReportId) {
		Optional<VenueReportVO> optional = repository.findById(venueReportId);
		return optional.orElse(null);
	}

	public List<VenueReportVO> getAll() {
		return repository.findAll();
	}

	public List<VenueReportVO> getByStatus(Byte reportStatus) {
		return repository.findByReportStatus(reportStatus);
	}

	public List<VenueReportVO> getByVenueOrderId(Integer venueOrderId) {
		return repository.findByVenueOrderId(venueOrderId);
	}

	public List<VenueReportVO> getByMemberId(Integer memberId) {
		return repository.findByMemberId(memberId);
	}

	public List<VenueReportVO> getByEmployeeId(Integer employeeId) {
		return repository.findByEmployeeId(employeeId);
	}

	// ===== 業務邏輯：審核通過（下架場地） =====
	/**
	 * 檢舉通過：更新檢舉紀錄狀態，並將對應場地訂單所屬的場地 venueStatus 切換為下架。
	 */
	@Transactional
	public void approve(Integer venueReportId, Integer employeeId) {
		VenueReportVO venueReport = getOneVenueReport(venueReportId);
		if (venueReport == null)
			return;
		venueReport.setReportStatus(STATUS_APPROVED);
		venueReport.setEmployeeId(employeeId);
		venueReport.setHandledAt(LocalDateTime.now());
		repository.save(venueReport);

		VenueOrderVO venueOrder = venueOrderRepository.findById(venueReport.getVenueOrderId()).orElse(null);
		if (venueOrder != null && venueOrder.getVenueVO() != null) {
			VenueVO venue = venueOrder.getVenueVO();
			venue.setVenueStatus(VENUE_STATUS_INACTIVE);
			venueRepository.save(venue);
		}
	}

	// ===== 業務邏輯：審核未通過 =====
	/**
	 * 檢舉未通過：只更新檢舉紀錄狀態，場地狀態不受影響。
	 */
	@Transactional
	public void reject(Integer venueReportId, Integer employeeId) {
		VenueReportVO venueReport = getOneVenueReport(venueReportId);
		if (venueReport == null)
			return;
		venueReport.setReportStatus(STATUS_REJECTED);
		venueReport.setEmployeeId(employeeId);
		venueReport.setHandledAt(LocalDateTime.now());
		repository.save(venueReport);
	}
}
