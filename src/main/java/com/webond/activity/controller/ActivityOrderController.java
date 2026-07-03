package com.webond.activity.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.webond.activity.model.ActivityOrderService;
import com.webond.activity.model.ActivityOrderVO;

@RestController
@RequestMapping("/api/activity-orders")
@CrossOrigin(origins = "*")
public class ActivityOrderController {

	@Autowired
	private ActivityOrderService orderSvc;

	// 查詢全部
	@GetMapping
	public ResponseEntity<List<ActivityOrderVO>> getAll() {
		return ResponseEntity.ok(orderSvc.getAll());
	}

	// 查詢單筆
	@GetMapping("/{id}")
	public ResponseEntity<?> getOne(@PathVariable Integer id) {
		ActivityOrderVO orderVO = orderSvc.getOneOrder(id);

		if (orderVO == null) {
			return ResponseEntity.badRequest().body("查無此訂單");
		}

		return ResponseEntity.ok(orderVO);
	}

	// 新增訂單
	@PostMapping
	public ResponseEntity<ActivityOrderVO> createOrder(@RequestBody ActivityOrderVO orderVO) {
		return ResponseEntity.ok(orderSvc.addOrder(orderVO));
	}

	// 修改整筆訂單
	@PutMapping("/{id}")
	public ResponseEntity<?> updateOrder(@PathVariable Integer id, @RequestBody ActivityOrderVO formVO) {

		ActivityOrderVO orderVO = orderSvc.getOneOrder(id);

		if (orderVO == null) {
			return ResponseEntity.badRequest().body("查無此訂單");
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

	// 修改訂單狀態
	@PutMapping("/{id}/status")
	public ResponseEntity<?> updateStatus(@PathVariable Integer id, @RequestParam Byte status) {
		try {
			return ResponseEntity.ok(orderSvc.updateOrderStatus(id, status));
		} catch (Exception e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		}
	}

	// 刪除
	@DeleteMapping("/{id}")
	public ResponseEntity<String> deleteOrder(@PathVariable Integer id) {
		orderSvc.deleteOrder(id);
		return ResponseEntity.ok("訂單已刪除");
	}
}