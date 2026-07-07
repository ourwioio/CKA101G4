package com.webond.venue.util;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.webond.venue.dto.VenueOrderQueryDTO;
import com.webond.venue.model.VenueOrderVO;

import jakarta.persistence.criteria.Predicate;

public class VenueOrderSpecification {

	public static Specification<VenueOrderVO> search(VenueOrderQueryDTO params) {
		return (root, query, cb) -> {
			List<Predicate> predicates = new ArrayList<>();

			if (params.getVenueOrderId() != null) {
				predicates.add(cb.equal(root.get("venueOrderId"), params.getVenueOrderId()));
			}

			if (params.getCreatedAtStart() != null) {
				// 轉成當天 00:00:00
				LocalDateTime startDateTime = params.getCreatedAtStart().atStartOfDay();
				predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), startDateTime));
			}
			if (params.getCreatedAtEnd() != null) {
				// 轉成隔天 00:00:00，用「小於」涵蓋整個結束日當天
				LocalDateTime endDateTime = params.getCreatedAtEnd().plusDays(1).atStartOfDay();
				predicates.add(cb.lessThan(root.get("createdAt"), endDateTime));
			}
			if (params.getOrderStatus() != null) {
				predicates.add(cb.equal(root.get("orderStatus"), params.getOrderStatus()));
			}
			if (params.getPayoutAmount() != null) {
				predicates.add(cb.equal(root.get("payoutAmount"), params.getPayoutAmount()));
			}
			if (params.getRefundStatus() != null) {
				predicates.add(cb.equal(root.get("refundStatus"), params.getRefundStatus()));
			}
			if (params.getEmployeeId() != null) {
				predicates.add(cb.equal(root.get("empVO").get("employeeId"), params.getEmployeeId()));
			}

			return cb.and(predicates.toArray(new Predicate[0]));
		};
	}
}