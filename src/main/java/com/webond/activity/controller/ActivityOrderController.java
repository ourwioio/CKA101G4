package com.webond.activity.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.webond.activity.model.ActivityOrderService;
import com.webond.activity.model.ActivityOrderVO;

@RestController
@RequestMapping("/api/activity-orders")
@CrossOrigin(origins = "*")
public class ActivityOrderController {

	@Autowired
	private ActivityOrderService orderSvc;

	@GetMapping
	public ResponseEntity<List<ActivityOrderVO>> getAll() {
		return ResponseEntity.ok(orderSvc.getAll());
	}

	@GetMapping("/{id}")
	public ResponseEntity<?> getOne(@PathVariable Integer id) {
		ActivityOrderVO orderVO = orderSvc.getOneOrder(id);

		if (orderVO == null) {
			return ResponseEntity.badRequest().body("\u67e5\u7121\u8a02\u55ae\u8cc7\u6599");
		}

		return ResponseEntity.ok(orderVO);
	}

	@PostMapping
	public ResponseEntity<ActivityOrderVO> createOrder(@RequestBody ActivityOrderVO orderVO) {
		return ResponseEntity.ok(orderSvc.addOrder(orderVO));
	}

	@PutMapping("/{id}")
	public ResponseEntity<?> updateOrder(@PathVariable Integer id, @RequestBody ActivityOrderVO formVO) {
		ActivityOrderVO orderVO = orderSvc.getOneOrder(id);

		if (orderVO == null) {
			return ResponseEntity.badRequest().body("\u67e5\u7121\u8a02\u55ae\u8cc7\u6599");
		}

		orderVO.setActivityId(formVO.getActivityId());
		orderVO.setBuyerMemberId(formVO.getBuyerMemberId());
		orderVO.setEmployeeId(formVO.getEmployeeId());
		orderVO.setOrderStatus(formVO.getOrderStatus());
		orderVO.setBookingCount(formVO.getBookingCount());
		orderVO.setActivityPrice(formVO.getActivityPrice());
		orderVO.setTotalAmount(formVO.getTotalAmount());
		orderVO.setOrderNote(formVO.getOrderNote());
		orderVO.setActivityPaymentMethod(formVO.getActivityPaymentMethod());
		orderVO.setPaidAt(formVO.getPaidAt());
		orderVO.setActivityCompletedAt(formVO.getActivityCompletedAt());
		orderVO.setBuyerRateSeller(formVO.getBuyerRateSeller());
		orderVO.setBuyerReviewComment(formVO.getBuyerReviewComment());
		orderVO.setBuyerReviewedAt(formVO.getBuyerReviewedAt());
		orderVO.setSellerRateBuyer(formVO.getSellerRateBuyer());
		orderVO.setSellerReviewComment(formVO.getSellerReviewComment());
		orderVO.setSellerReviewedAt(formVO.getSellerReviewedAt());
		orderVO.setPayoutAmount(formVO.getPayoutAmount());
		orderVO.setRefundReason(formVO.getRefundReason());
		orderVO.setRefundStatus(formVO.getRefundStatus());

		return ResponseEntity.ok(orderSvc.updateOrder(orderVO));
	}

	@PutMapping("/{id}/status")
	public ResponseEntity<?> updateStatus(@PathVariable Integer id, @RequestParam Byte status) {
		try {
			return ResponseEntity.ok(orderSvc.updateOrderStatus(id, status));
		} catch (Exception e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		}
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<String> deleteOrder(@PathVariable Integer id) {
		orderSvc.deleteOrder(id);
		return ResponseEntity.ok("\u8a02\u55ae\u5df2\u522a\u9664");
	}
}
