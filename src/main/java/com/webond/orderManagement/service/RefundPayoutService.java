package com.webond.orderManagement.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.webond.activity.model.ActivityOrderVO;
import com.webond.activity.repository.ActivityOrderRepository;
import com.webond.employee.model.EmployeeVO;
import com.webond.employee.repository.EmployeeRepository;
import com.webond.orderManagement.dto.RefundPayoutDTO;
import com.webond.orderManagement.dto.RefundPayoutQueryDTO;
import com.webond.service.model.ServiceOrderVO;
import com.webond.service.repository.ServiceOrderRepository;
import com.webond.venue.model.VenueOrderVO;
import com.webond.venue.repository.VenueOrderRepository;

@Service
public class RefundPayoutService {

	@Autowired
	private ServiceOrderRepository serviceOrderRepository;

	@Autowired
	private ActivityOrderRepository activityOrderRepository;

	@Autowired
	private VenueOrderRepository venueOrderRepository;

	@Autowired
	private EmployeeRepository employeeRepository;

	// ============ 退款所有明細 ============
	public List<RefundPayoutDTO> findRefundList(RefundPayoutQueryDTO query) {
		query.resolveRange();
		LocalDateTime start = query.getStartDate();
		LocalDateTime end = query.getEndDate();
		String orderType = query.getSourceType();

		Map<Integer, String> empNameMap = buildEmployeeNameMap();
		List<RefundPayoutDTO> result = new ArrayList<>();

		if (orderType == null || orderType.isEmpty() || orderType.equals("ONE_ON_ONE")) {
			serviceOrderRepository.findAll().stream()
					.filter(o -> o.getRefundStatus() != null && o.getRefundStatus() > 0)
					.filter(o -> isInRange(o.getHandledAt(), start, end))
					.forEach(o -> result.add(mapFromServiceOrder(o, empNameMap)));
		}

		if (orderType == null || orderType.isEmpty() || orderType.equals("GROUP")) {
			activityOrderRepository.findAll().stream()
					.filter(o -> o.getRefundStatus() != null && o.getRefundStatus() > 0)
					.filter(o -> isInRange(o.getApprovedAt(), start, end))
					.forEach(o -> result.add(mapFromActivityOrder(o, empNameMap)));
		}

		if (orderType == null || orderType.isEmpty() || orderType.equals("VENUE")) {
			venueOrderRepository.findAll().stream().filter(o -> o.getRefundStatus() != null && o.getRefundStatus() != 2) // 2=未申請
																															// 排除
					.filter(o -> isInRange(o.getHandledAt(), start, end))
					.forEach(o -> result.add(mapFromVenueOrder(o)));
		}

		return result.stream().sorted(
				Comparator.comparing(RefundPayoutDTO::getHandledAt, Comparator.nullsLast(Comparator.reverseOrder())))
				.toList();
	}

	// ============ 撥款所有明細 ============
	public List<RefundPayoutDTO> findPayoutList(RefundPayoutQueryDTO query) {
		query.resolveRange();
		LocalDateTime start = query.getStartDate();
		LocalDateTime end = query.getEndDate();
		String orderType = query.getSourceType();

		Map<Integer, String> empNameMap = buildEmployeeNameMap();
		List<RefundPayoutDTO> result = new ArrayList<>();

		if (orderType == null || orderType.isEmpty() || orderType.equals("ONE_ON_ONE")) {
			serviceOrderRepository.findAll().stream()
					.filter(o -> o.getPayoutStatus() != null && o.getPayoutStatus() == 1)
					.filter(o -> isInRange(o.getHandledAt(), start, end))
					.forEach(o -> result.add(mapFromServiceOrder(o, empNameMap)));
		}

		if (orderType == null || orderType.isEmpty() || orderType.equals("GROUP")) {
			activityOrderRepository.findAll().stream().filter(o -> Boolean.TRUE.equals(o.getPayoutAmount()))
					.filter(o -> isInRange(o.getApprovedAt(), start, end))
					.forEach(o -> result.add(mapFromActivityOrder(o, empNameMap)));
		}

		if (orderType == null || orderType.isEmpty() || orderType.equals("VENUE")) {
			venueOrderRepository.findAll().stream().filter(o -> o.getPayoutAmount() != null && o.getPayoutAmount() == 1)
					.filter(o -> isInRange(o.getHandledAt(), start, end))
					.forEach(o -> result.add(mapFromVenueOrder(o)));
		}

		return result.stream().sorted(
				Comparator.comparing(RefundPayoutDTO::getHandledAt, Comparator.nullsLast(Comparator.reverseOrder())))
				.toList();
	}

	// 預先撈全部員工姓名，避免每筆訂單各查一次DB
	private Map<Integer, String> buildEmployeeNameMap() {
		return employeeRepository.findAll().stream()
				.collect(Collectors.toMap(EmployeeVO::getEmployeeId, EmployeeVO::getEmpName));
	}

	// ============ 轉換方法 ============
	private RefundPayoutDTO mapFromServiceOrder(ServiceOrderVO s, Map<Integer, String> empNameMap) {
		RefundPayoutDTO dto = new RefundPayoutDTO();
		dto.setOrderType("ONE_ON_ONE");
		dto.setOrderTypeName("一對一服務");
		dto.setOrderId(s.getServiceOrderId());
		dto.setMemberId(s.getBuyerMemberId());
		dto.setTotalAmount(s.getTotalAmount());
		dto.setRefundStatus(s.getRefundStatus());
		dto.setRefundAmount(s.getRefundAmount());
		dto.setRefundReason(s.getCancelReason());
		dto.setPayoutDone(s.getPayoutStatus() != null && s.getPayoutStatus() == 1);
		dto.setEmployeeId(s.getEmployeeId());
		dto.setEmployeeName(s.getEmployeeId() != null ? empNameMap.get(s.getEmployeeId()) : null);
		dto.setHandledAt(s.getHandledAt());
		dto.setCreatedAt(s.getCreatedAt());
		return dto;
	}

	private RefundPayoutDTO mapFromActivityOrder(ActivityOrderVO a, Map<Integer, String> empNameMap) {
		RefundPayoutDTO dto = new RefundPayoutDTO();
		dto.setOrderType("GROUP");
		dto.setOrderTypeName("揪團活動");
		dto.setOrderId(a.getActivityOrderId());
		dto.setMemberId(a.getBuyerMemberId());
		dto.setTotalAmount(a.getTotalAmount());
		dto.setRefundStatus(a.getRefundStatus());
		dto.setRefundAmount(a.getRefundStatus() != null && a.getRefundStatus() == 1 ? a.getTotalAmount() : 0);
		dto.setRefundReason(a.getRefundReason());
		dto.setPayoutDone(Boolean.TRUE.equals(a.getPayoutAmount()));
		dto.setEmployeeId(a.getEmployeeId());
		dto.setEmployeeName(a.getEmployeeId() != null ? empNameMap.get(a.getEmployeeId()) : null);
		dto.setHandledAt(a.getApprovedAt());
		dto.setCreatedAt(a.getPaidAt());
		return dto;
	}

	private RefundPayoutDTO mapFromVenueOrder(VenueOrderVO v) {
		RefundPayoutDTO dto = new RefundPayoutDTO();
		dto.setOrderType("VENUE");
		dto.setOrderTypeName("場地租借");
		dto.setOrderId(v.getVenueOrderId());
		dto.setMemberId(v.getMember() != null ? v.getMember().getMemberId() : null);
		dto.setTotalAmount(v.getTotalAmount());
		dto.setRefundStatus(v.getRefundStatus());
		dto.setRefundAmount(v.getRefundStatus() != null && v.getRefundStatus() == 1 ? v.getTotalAmount() : 0);
		dto.setRefundReason(v.getRefundReason());
		dto.setPayoutDone(v.getPayoutAmount() != null && v.getPayoutAmount() == 1);
		dto.setEmployeeId(v.getEmpVO() != null ? v.getEmpVO().getEmployeeId() : null);
		dto.setEmployeeName(v.getEmpVO() != null ? v.getEmpVO().getEmpName() : null);
		dto.setHandledAt(v.getHandledAt());
		dto.setCreatedAt(v.getCreatedAt());
		return dto;
	}

	private boolean isInRange(LocalDateTime target, LocalDateTime start, LocalDateTime end) {
		if (target == null)
			return true;
		if (start != null && target.isBefore(start))
			return false;
		if (end != null && target.isAfter(end))
			return false;
		return true;
	}
}
