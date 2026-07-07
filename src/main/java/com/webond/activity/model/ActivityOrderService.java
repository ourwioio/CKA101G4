package com.webond.activity.model;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.webond.activity.repository.ActivityOrderRepository;

@Service
@Transactional
public class ActivityOrderService {

	private static final Byte ORDER_STATUS_ACTIVE = 0;
	private static final Byte ORDER_STATUS_CANCELLED = 1;
	private static final Byte ORDER_STATUS_REFUND_REQUESTED = 2;
	private static final Byte ORDER_STATUS_PENDING_REVIEW = 3;
	private static final Byte ORDER_STATUS_PENDING_PAYMENT = 4;
	private static final Byte REFUND_STATUS_REQUESTED = 0;

	@Autowired
	private ActivityOrderRepository orderRepo;

	// 查詢全部
	public List<ActivityOrderVO> getAll() {
		return orderRepo.findAll();
	}

	// 查詢單筆
	public ActivityOrderVO getOneOrder(Integer activityOrderId) {
		Optional<ActivityOrderVO> optional = orderRepo.findById(activityOrderId);
		return optional.orElse(null);
	}

	// 新增
	public ActivityOrderVO addOrder(ActivityOrderVO orderVO) {
		return orderRepo.save(orderVO);
	}

	// 修改
	public ActivityOrderVO updateOrder(ActivityOrderVO orderVO) {
		return orderRepo.save(orderVO);
	}

	// 新增/修改共用
	public ActivityOrderVO saveOrder(ActivityOrderVO orderVO) {
		return orderRepo.save(orderVO);
	}

	// 查詢會員自己的訂單
	public List<ActivityOrderVO> getOrdersByBuyerMemberId(Integer buyerMemberId) {
		return orderRepo.findByBuyerMemberId(buyerMemberId);
	}

	public boolean hasActiveOrder(Integer activityId, Integer buyerMemberId) {
		return orderRepo.existsByActivityIdAndBuyerMemberIdAndOrderStatusIn(activityId, buyerMemberId,
				List.of(ORDER_STATUS_ACTIVE, ORDER_STATUS_REFUND_REQUESTED, ORDER_STATUS_PENDING_REVIEW,
						ORDER_STATUS_PENDING_PAYMENT));
	}

	public boolean hasPaidOrder(Integer activityId, Integer buyerMemberId) {
		return orderRepo.existsByActivityIdAndBuyerMemberIdAndOrderStatus(activityId, buyerMemberId,
				ORDER_STATUS_ACTIVE);
	}

	// 修改訂單狀態(API使用)
	public ActivityOrderVO updateOrderStatus(Integer activityOrderId, Byte orderStatus) {

		ActivityOrderVO orderVO = getOneOrder(activityOrderId);

		if (orderVO == null) {
			throw new RuntimeException("查無此訂單");
		}

		orderVO.setOrderStatus(orderStatus);

		return orderRepo.save(orderVO);
	}

	// 刪除
	public void deleteOrder(Integer activityOrderId) {
		orderRepo.deleteById(activityOrderId);
	}

	// 查詢某活動的所有報名訂單
	public List<ActivityOrderVO> getOrdersByActivityId(Integer activityId) {
		return orderRepo.findByActivityId(activityId);
	}

	public ActivityOrderVO cancelOrder(Integer activityOrderId, Integer buyerMemberId) {
		ActivityOrderVO orderVO = getOneOrder(activityOrderId);

		if (orderVO == null || !orderVO.getBuyerMemberId().equals(buyerMemberId)) {
			return null;
		}

		if (!ORDER_STATUS_ACTIVE.equals(orderVO.getOrderStatus())
				&& !ORDER_STATUS_PENDING_REVIEW.equals(orderVO.getOrderStatus())
				&& !ORDER_STATUS_PENDING_PAYMENT.equals(orderVO.getOrderStatus())) {
			return orderVO;
		}

		orderVO.setOrderStatus(ORDER_STATUS_CANCELLED);
		return orderRepo.save(orderVO);
	}

	public ActivityOrderVO requestRefund(Integer activityOrderId, Integer buyerMemberId, String refundReason) {
		ActivityOrderVO orderVO = getOneOrder(activityOrderId);

		if (orderVO == null || !orderVO.getBuyerMemberId().equals(buyerMemberId)) {
			return null;
		}

		if (!ORDER_STATUS_ACTIVE.equals(orderVO.getOrderStatus())) {
			return orderVO;
		}

		orderVO.setOrderStatus(ORDER_STATUS_REFUND_REQUESTED);
		orderVO.setRefundStatus(REFUND_STATUS_REQUESTED);
		orderVO.setRefundReason(refundReason);
		return orderRepo.save(orderVO);
	}

	public Integer getActiveBookingCount(Integer activityId) {
		return orderRepo.sumBookingCountByActivityIdAndOrderStatus(activityId, ORDER_STATUS_ACTIVE);
	}

	public Integer getPendingReviewCount(Integer activityId) {
		Long count = orderRepo.countByActivityIdAndOrderStatus(activityId, ORDER_STATUS_PENDING_REVIEW);
		return count == null ? 0 : count.intValue();
	}

	public ActivityOrderVO approveOrder(Integer activityOrderId, Integer employeeId) {
		ActivityOrderVO orderVO = getOneOrder(activityOrderId);

		if (orderVO == null || !ORDER_STATUS_PENDING_REVIEW.equals(orderVO.getOrderStatus())) {
			return orderVO;
		}

		orderVO.setOrderStatus(ORDER_STATUS_PENDING_PAYMENT);
		orderVO.setEmployeeId(employeeId);
		return orderRepo.save(orderVO);
	}

	public ActivityOrderVO approveOrderByHost(Integer activityOrderId) {
		ActivityOrderVO orderVO = getOneOrder(activityOrderId);

		if (orderVO == null || !ORDER_STATUS_PENDING_REVIEW.equals(orderVO.getOrderStatus())) {
			return orderVO;
		}

		orderVO.setOrderStatus(ORDER_STATUS_PENDING_PAYMENT);
		return orderRepo.save(orderVO);
	}

	public ActivityOrderVO rejectOrder(Integer activityOrderId, Integer employeeId) {
		ActivityOrderVO orderVO = getOneOrder(activityOrderId);

		if (orderVO == null || !ORDER_STATUS_PENDING_REVIEW.equals(orderVO.getOrderStatus())) {
			return orderVO;
		}

		orderVO.setOrderStatus(ORDER_STATUS_CANCELLED);
		orderVO.setEmployeeId(employeeId);
		return orderRepo.save(orderVO);
	}

	public ActivityOrderVO rejectOrderByHost(Integer activityOrderId) {
		ActivityOrderVO orderVO = getOneOrder(activityOrderId);

		if (orderVO == null || !ORDER_STATUS_PENDING_REVIEW.equals(orderVO.getOrderStatus())) {
			return orderVO;
		}

		orderVO.setOrderStatus(ORDER_STATUS_CANCELLED);
		return orderRepo.save(orderVO);
	}

	public void rejectPendingOrdersByActivity(Integer activityId, Integer employeeId) {
		List<ActivityOrderVO> pendingOrders = orderRepo.findByActivityIdAndOrderStatus(activityId,
				ORDER_STATUS_PENDING_REVIEW);

		for (ActivityOrderVO orderVO : pendingOrders) {
			orderVO.setOrderStatus(ORDER_STATUS_CANCELLED);
			orderVO.setEmployeeId(employeeId);
			orderRepo.save(orderVO);
		}
	}

	public void rejectPendingOrdersByFullActivity(Integer activityId) {
		List<ActivityOrderVO> pendingOrders = orderRepo.findByActivityIdAndOrderStatus(activityId,
				ORDER_STATUS_PENDING_REVIEW);

		for (ActivityOrderVO orderVO : pendingOrders) {
			orderVO.setOrderStatus(ORDER_STATUS_CANCELLED);
			orderRepo.save(orderVO);
		}
	}

	public ActivityOrderVO payOrder(Integer activityOrderId, Integer buyerMemberId, Byte paymentMethod) {
		ActivityOrderVO orderVO = getOneOrder(activityOrderId);

		if (orderVO == null || !orderVO.getBuyerMemberId().equals(buyerMemberId)) {
			return null;
		}

		if (!ORDER_STATUS_PENDING_PAYMENT.equals(orderVO.getOrderStatus())) {
			return orderVO;
		}

		orderVO.setActivityPaymentMethod(paymentMethod);
		orderVO.setOrderStatus(ORDER_STATUS_ACTIVE);
		orderVO.setPaidAt(java.time.LocalDateTime.now());
		return orderRepo.save(orderVO);
	}

}
