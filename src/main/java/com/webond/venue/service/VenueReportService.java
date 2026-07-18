package com.webond.venue.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.webond.member.model.MemberVO;
import com.webond.member.repository.MemberRepository;
import com.webond.member.service.MemberServiceLoie;
import com.webond.venue.model.VenueOrderVO;
import com.webond.venue.model.VenueReportVO;
import com.webond.venue.model.VenueVO;
import com.webond.venue.repository.VenueOrderRepository;
import com.webond.venue.repository.VenueReportRepository;
import com.webond.venue.repository.VenueRepository;

@Service("venueReportService")
public class VenueReportService {

	// ===== 檢舉處理狀態 =====
	public static final byte STATUS_REVIEWING = 0; // 審核中
	public static final byte STATUS_APPROVED = 1; // 審核通過（檢舉成立）
	public static final byte STATUS_REJECTED = 2; // 審核未通過

	// ===== 場地狀態 =====
	public static final byte VENUE_STATUS_INACTIVE = 0; // 下架
	public static final byte VENUE_STATUS_ACTIVE = 1; // 上架
	public static final byte VENUE_STATUS_PENDING = 2; // 待審核

	public static final Map<Byte, String> STATUS_LABEL_MAP = new LinkedHashMap<>();
	static {
		STATUS_LABEL_MAP.put(STATUS_REVIEWING, "審核中");
		STATUS_LABEL_MAP.put(STATUS_APPROVED, "審核通過");
		STATUS_LABEL_MAP.put(STATUS_REJECTED, "審核未通過");
	}

	@Autowired
	private VenueReportRepository venueReportRepository;

	@Autowired
	private VenueOrderRepository venueOrderRepository;

	@Autowired
	private VenueRepository venueRepository;

	@Autowired
	private MemberRepository memberRepository;
	
	@Autowired
	MemberServiceLoie memberServiceLoie;

	@Autowired
	private VenueReviewService venueReviewService;

	// ===== 前台：會員送出檢舉 =====
	public void addVenueReport(VenueReportVO venueReportVO) {
		venueReportVO.setVenueReportId(null); // 確保走 INSERT
		venueReportVO.setSerReportTime(LocalDateTime.now());
		venueReportVO.setReportStatus(STATUS_REVIEWING);
		venueReportVO.setEmployeeId(null);
		venueReportVO.setHandledAt(null);
		venueReportRepository.save(venueReportVO);
	}

	// ===== 防重複檢舉：同一筆訂單是否已有審核中的檢舉 =====
	public boolean hasReviewingReport(Integer venueOrderId) {
		return venueReportRepository.findByVenueOrderId(venueOrderId).stream()
				.anyMatch(report -> report.getReportStatus() != null && report.getReportStatus() == STATUS_REVIEWING);
	}

	// ===== 防重複檢舉：同一筆訂單是否已經檢舉過（不論審核結果，每筆訂單僅能檢舉一次）=====
	public boolean hasAnyReport(Integer venueOrderId) {
		return !venueReportRepository.findByVenueOrderId(venueOrderId).isEmpty();
	}

	// ===== 批次查詢：這些訂單裡哪些已經檢舉過（供列表頁一次判斷用）=====
	public Set<Integer> getReportedOrderIds(List<Integer> venueOrderIds) {
		if (venueOrderIds == null || venueOrderIds.isEmpty()) {
			return Collections.emptySet();
		}
		return venueReportRepository.findByVenueOrderIdIn(venueOrderIds).stream()
				.map(VenueReportVO::getVenueOrderId)
				.collect(Collectors.toSet());
	}

	// ===== 批次查詢：這些訂單各自的檢舉內容（供列表頁點開查看用，每筆訂單僅能檢舉一次）=====
	public Map<Integer, VenueReportVO> getReportsByOrderIds(List<Integer> venueOrderIds) {
		if (venueOrderIds == null || venueOrderIds.isEmpty()) {
			return Collections.emptyMap();
		}
		return venueReportRepository.findByVenueOrderIdIn(venueOrderIds).stream()
				.collect(Collectors.toMap(VenueReportVO::getVenueOrderId, r -> r));
	}

	/**
	 * 後台：檢舉成立 ① 檢舉狀態 → 1（審核通過） ② 場地提供者違規點數 +1（停權判斷由會員模組負責） ③
	 * VENUE_REVIEW.reviewStatus → 0（審核中），清空審核人／備註／審核時間 ④ VENUE.venueStatus →
	 * 2（待審核），待提供者修改資料後重新審核
	 */
	@Transactional
	public void approve(Integer venueReportId, Integer employeeId) {
		VenueReportVO venueReport = venueReportRepository.findById(venueReportId).orElse(null);
		if (venueReport == null) {
			return;
		}
		if (venueReport.getReportStatus() != null && venueReport.getReportStatus() != STATUS_REVIEWING) {
			return; // 已處理過則鎖定，不可重複審核
		}

		// ① 更新檢舉紀錄
		venueReport.setReportStatus(STATUS_APPROVED);
		venueReport.setEmployeeId(employeeId);
		venueReport.setHandledAt(LocalDateTime.now());
		venueReportRepository.save(venueReport);

		VenueOrderVO venueOrder = venueOrderRepository.findById(venueReport.getVenueOrderId()).orElse(null);
		if (venueOrder == null || venueOrder.getVenueVO() == null) {
			return;
		}
		VenueVO venueVO = venueOrder.getVenueVO();

		// ② 場地提供者違規點數 +1
		MemberVO provider = venueVO.getMember();
		if (provider != null) {
			int currentPoints = provider.getReportPoints() != null ? provider.getReportPoints() : 0;
			provider.setReportPoints(currentPoints + 1);
			memberRepository.save(provider);
		}

		// ③ 場地審核紀錄退回審核中
		venueReviewService.resetToReviewing(venueVO.getVenueId());

		// ④ 場地狀態改為待審核
		venueVO.setVenueStatus(VENUE_STATUS_PENDING);
		venueRepository.save(venueVO);
		
		// 修改會員狀態
		Integer memberReportPoint = provider.getReportPoints();
		if (memberReportPoint >= 5) {
			memberServiceLoie.updateMember(provider);
		}
	}

	// ===== 後台：檢舉不成立，場地與會員皆不受影響 =====
	@Transactional
	public void reject(Integer venueReportId, Integer employeeId) {
		VenueReportVO venueReport = venueReportRepository.findById(venueReportId).orElse(null);
		if (venueReport == null) {
			return;
		}
		if (venueReport.getReportStatus() != null && venueReport.getReportStatus() != STATUS_REVIEWING) {
			return;
		}

		venueReport.setReportStatus(STATUS_REJECTED);
		venueReport.setEmployeeId(employeeId);
		venueReport.setHandledAt(LocalDateTime.now());
		venueReportRepository.save(venueReport);
	}

	// ===== 查詢 =====
	public List<VenueReportVO> getAll() {
		return venueReportRepository.findAllByOrderBySerReportTimeDesc();
	}

	public List<VenueReportVO> getByStatus(Byte reportStatus) {
		return venueReportRepository.findByReportStatus(reportStatus);
	}

	public VenueReportVO getOneVenueReport(Integer venueReportId) {
		return venueReportRepository.findById(venueReportId).orElse(null);
	}

	public VenueOrderVO getVenueOrder(Integer venueOrderId) {
		return venueOrderRepository.findById(venueOrderId).orElse(null);
	}

	// ===== 複合查詢：處理狀態 + 檢舉內容關鍵字 + 場地名稱 + 檢舉時間區間，任意組合 =====
	public List<VenueReportVO> search(Byte reportStatus, String keyword, String venueName, LocalDate startDate,
			LocalDate endDate) {

		List<VenueReportVO> list;

		// 有完整日期區間先由 DB 撈基礎清單，否則撈全部
		if (startDate != null && endDate != null) {
			list = venueReportRepository.findBySerReportTimeBetween(startDate.atStartOfDay(),
					endDate.atTime(LocalTime.MAX));
		} else {
			list = venueReportRepository.findAllByOrderBySerReportTimeDesc();
		}

		if (reportStatus != null) {
			list = list.stream().filter(report -> reportStatus.equals(report.getReportStatus())).toList();
		}

		if (keyword != null && !keyword.trim().isEmpty()) {
			String lowerKeyword = keyword.trim().toLowerCase();
			list = list.stream().filter(report -> report.getSerReportCom() != null
					&& report.getSerReportCom().toLowerCase().contains(lowerKeyword)).toList();
		}

		if (venueName != null && !venueName.trim().isEmpty()) {
			String lowerName = venueName.trim().toLowerCase();
			list = list.stream().filter(report -> {
				VenueOrderVO order = venueOrderRepository.findById(report.getVenueOrderId()).orElse(null);
				return order != null && order.getVenueVO() != null && order.getVenueVO().getVenueName() != null
						&& order.getVenueVO().getVenueName().toLowerCase().contains(lowerName);
			}).toList();
		}

		return list;
	}

	// ===== 提供「檢舉編號 → 場地名稱」對照表，給列表頁顯示用 =====
	public Map<Integer, String> getVenueNameMap(List<VenueReportVO> list) {
		Map<Integer, String> map = new LinkedHashMap<>();
		for (VenueReportVO report : list) {
			VenueOrderVO order = venueOrderRepository.findById(report.getVenueOrderId()).orElse(null);
			String name = (order != null && order.getVenueVO() != null) ? order.getVenueVO().getVenueName() : "-";
			map.put(report.getVenueReportId(), name);
		}
		return map;
	}
}