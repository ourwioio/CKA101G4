package com.webond.activity.model;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.webond.activity.repository.ActivityOrderRepository;

@Service
@Transactional
public class ActivityOrderService {

	private static final Byte ORDER_STATUS_ACTIVE = 0;
	private static final Byte ORDER_STATUS_CANCELLED = 1;
	private static final Byte ORDER_STATUS_PENDING_REVIEW = 2;
	private static final Byte ORDER_STATUS_PENDING_PAYMENT = 3;
	private static final Byte ORDER_STATUS_COMPLETED = 4;
	private static final Byte REFUND_STATUS_NONE = 0;
	private static final Byte REFUND_STATUS_REQUESTED = 1;
	private static final Byte REFUND_STATUS_DONE = 2;
	private static final int PAYMENT_TIMEOUT_SECONDS = 60;
	private static final List<Byte> CANCELLABLE_ORDER_STATUSES = List.of(
			ORDER_STATUS_ACTIVE, ORDER_STATUS_PENDING_REVIEW, ORDER_STATUS_PENDING_PAYMENT);

	@Autowired
	private ActivityOrderRepository orderRepo;

	public List<ActivityOrderVO> getAll() {
		return orderRepo.findAll(Sort.by(Sort.Direction.DESC, "activityOrderId"));
	}

	public ActivityOrderVO getOneOrder(Integer activityOrderId) {
		Optional<ActivityOrderVO> optional = orderRepo.findById(activityOrderId);
		return optional.orElse(null);
	}

	public ActivityOrderVO addOrder(ActivityOrderVO orderVO) {
		applyDefaultRefundStatus(orderVO);
		return orderRepo.save(orderVO);
	}

	public ActivityOrderVO updateOrder(ActivityOrderVO orderVO) {
		applyDefaultRefundStatus(orderVO);
		return orderRepo.save(orderVO);
	}

	public ActivityOrderVO saveOrder(ActivityOrderVO orderVO) {
		applyDefaultRefundStatus(orderVO);
		return orderRepo.save(orderVO);
	}

	public List<ActivityOrderVO> getOrdersByBuyerMemberId(Integer buyerMemberId) {
		return orderRepo.findByBuyerMemberId(buyerMemberId);
	}

	public boolean hasActiveOrder(Integer activityId, Integer buyerMemberId) {
		return orderRepo.existsByActivityIdAndBuyerMemberIdAndOrderStatusIn(activityId, buyerMemberId,
				List.of(ORDER_STATUS_ACTIVE, ORDER_STATUS_PENDING_REVIEW, ORDER_STATUS_PENDING_PAYMENT));
	}

	public boolean hasPaidOrder(Integer activityId, Integer buyerMemberId) {
		return orderRepo.existsByActivityIdAndBuyerMemberIdAndOrderStatus(activityId, buyerMemberId,
				ORDER_STATUS_ACTIVE);
	}

	public ActivityOrderVO updateOrderStatus(Integer activityOrderId, Byte orderStatus) {
		ActivityOrderVO orderVO = getOneOrder(activityOrderId);

		if (orderVO == null) {
			throw new RuntimeException("\u67e5\u7121\u6b64\u8a02\u55ae");
		}

		orderVO.setOrderStatus(orderStatus);
		return orderRepo.save(orderVO);
	}

	public void deleteOrder(Integer activityOrderId) {
		orderRepo.deleteById(activityOrderId);
	}

	public List<ActivityOrderVO> getOrdersByActivityId(Integer activityId) {
		return orderRepo.findByActivityId(activityId);
	}

	public List<ActivityOrderVO> getReviewedOrdersByActivityId(Integer activityId) {
		return orderRepo.findByActivityIdAndBuyerReviewCommentIsNotNullOrderByBuyerReviewedAtDesc(activityId);
	}

	/**
	 * Cancels all unfinished orders for an activity cancelled by the system.
	 * Paid orders enter the refund workflow; unpaid orders are simply cancelled.
	 */
	public int cancelOrdersByActivity(Integer activityId, String reason) {
		if (activityId == null) {
			return 0;
		}

		List<ActivityOrderVO> orders = orderRepo.findByActivityIdAndOrderStatusIn(
				activityId, CANCELLABLE_ORDER_STATUSES);
		for (ActivityOrderVO orderVO : orders) {
			applySystemCancellation(orderVO, reason);
		}
		orderRepo.saveAll(orders);
		return orders.size();
	}

	/**
	 * Cancels a disabled member's unfinished registrations for activities that
	 * have not ended. The affected activity IDs are returned for attendee sync.
	 */
	public Set<Integer> cancelOrdersByDisabledBuyer(Integer buyerMemberId, LocalDateTime now, String reason) {
		Set<Integer> affectedActivityIds = new HashSet<>();
		if (buyerMemberId == null || now == null) {
			return affectedActivityIds;
		}

		List<ActivityOrderVO> orders = orderRepo.findActiveOrdersByBuyerMemberId(
				buyerMemberId, CANCELLABLE_ORDER_STATUSES, now);
		for (ActivityOrderVO orderVO : orders) {
			applySystemCancellation(orderVO, reason);
			affectedActivityIds.add(orderVO.getActivityId());
		}
		orderRepo.saveAll(orders);
		return affectedActivityIds;
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

		if (ORDER_STATUS_ACTIVE.equals(orderVO.getOrderStatus())) {
			orderVO.setOrderStatus(ORDER_STATUS_CANCELLED);
			if (isFreeOrder(orderVO)) {
				orderVO.setRefundStatus(REFUND_STATUS_NONE);
				orderVO.setRefundReason("\u0030\u5143\u8a02\u55ae\u53d6\u6d88\uff0c\u7121\u9700\u9000\u6b3e");
			} else {
				orderVO.setRefundStatus(REFUND_STATUS_REQUESTED);
				orderVO.setRefundReason("\u6703\u54e1\u53d6\u6d88\u5df2\u4ed8\u6b3e\u6d3b\u52d5\uff0c\u7533\u8acb\u5168\u984d\u9000\u6b3e");
			}
			return orderRepo.save(orderVO);
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

		orderVO.setOrderStatus(ORDER_STATUS_CANCELLED);
		if (isFreeOrder(orderVO)) {
			orderVO.setRefundStatus(REFUND_STATUS_NONE);
			orderVO.setRefundReason("\u0030\u5143\u8a02\u55ae\u53d6\u6d88\uff0c\u7121\u9700\u9000\u6b3e");
		} else {
			orderVO.setRefundStatus(REFUND_STATUS_REQUESTED);
			orderVO.setRefundReason(refundReason);
		}
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
		orderVO.setApprovedAt(LocalDateTime.now());
		return orderRepo.save(orderVO);
	}

	public ActivityOrderVO approveOrderByHost(Integer activityOrderId) {
		ActivityOrderVO orderVO = getOneOrder(activityOrderId);

		if (orderVO == null || !ORDER_STATUS_PENDING_REVIEW.equals(orderVO.getOrderStatus())) {
			return orderVO;
		}

		orderVO.setOrderStatus(ORDER_STATUS_PENDING_PAYMENT);
		orderVO.setApprovedAt(LocalDateTime.now());
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
			orderVO.setRefundStatus(REFUND_STATUS_NONE);
			orderVO.setRefundReason("\u540d\u984d\u5df2\u6eff\uff0c\u7cfb\u7d71\u81ea\u52d5\u53d6\u6d88\uff0c\u672a\u4ed8\u6b3e\u7121\u9700\u9000\u6b3e");
			orderRepo.save(orderVO);
		}
	}

	public synchronized ActivityOrderVO payOrder(Integer activityOrderId, Integer buyerMemberId, Byte paymentMethod,
			boolean hasCapacity) {
		ActivityOrderVO orderVO = getOneOrder(activityOrderId);

		if (orderVO == null || !orderVO.getBuyerMemberId().equals(buyerMemberId)) {
			return null;
		}

		if (!ORDER_STATUS_PENDING_PAYMENT.equals(orderVO.getOrderStatus())) {
			return orderVO;
		}

		if (isPaymentExpired(orderVO)) {
			orderVO.setOrderStatus(ORDER_STATUS_CANCELLED);
			orderVO.setRefundStatus(REFUND_STATUS_NONE);
			orderVO.setRefundReason("\u4ed8\u6b3e\u903e\u6642\uff0c\u7cfb\u7d71\u81ea\u52d5\u53d6\u6d88\uff0c\u672a\u4ed8\u6b3e\u7121\u9700\u9000\u6b3e");
			return orderRepo.save(orderVO);
		}

		if (!hasCapacity) {
			orderVO.setOrderStatus(ORDER_STATUS_CANCELLED);
			orderVO.setRefundStatus(REFUND_STATUS_NONE);
			orderVO.setRefundReason("\u540d\u984d\u5df2\u6eff\uff0c\u7cfb\u7d71\u81ea\u52d5\u53d6\u6d88\uff0c\u672a\u4ed8\u6b3e\u7121\u9700\u9000\u6b3e");
			return orderRepo.save(orderVO);
		}

		orderVO.setActivityPaymentMethod(paymentMethod);
		orderVO.setOrderStatus(ORDER_STATUS_ACTIVE);
		orderVO.setPaidAt(LocalDateTime.now());
		return orderRepo.save(orderVO);
	}

	public void failPendingPaymentOrdersByFullActivity(Integer activityId) {
		List<ActivityOrderVO> pendingPaymentOrders = orderRepo.findByActivityIdAndOrderStatus(activityId,
				ORDER_STATUS_PENDING_PAYMENT);

		for (ActivityOrderVO orderVO : pendingPaymentOrders) {
			orderVO.setOrderStatus(ORDER_STATUS_CANCELLED);
			orderVO.setRefundStatus(REFUND_STATUS_NONE);
			orderVO.setRefundReason("\u540d\u984d\u5df2\u6eff\uff0c\u7cfb\u7d71\u81ea\u52d5\u53d6\u6d88\uff0c\u672a\u4ed8\u6b3e\u7121\u9700\u9000\u6b3e");
			orderRepo.save(orderVO);
		}
	}

	public int expireOverduePendingPaymentOrders() {
		for (ActivityOrderVO orderVO : orderRepo.findByOrderStatus(ORDER_STATUS_PENDING_PAYMENT)) {
			if (orderVO.getApprovedAt() == null) {
				orderVO.setApprovedAt(LocalDateTime.now());
				orderRepo.save(orderVO);
			}
		}

		LocalDateTime deadline = LocalDateTime.now().minusSeconds(PAYMENT_TIMEOUT_SECONDS);
		List<ActivityOrderVO> overdueOrders = orderRepo.findByOrderStatusAndApprovedAtBefore(
				ORDER_STATUS_PENDING_PAYMENT, deadline);

		for (ActivityOrderVO orderVO : overdueOrders) {
			orderVO.setOrderStatus(ORDER_STATUS_CANCELLED);
			orderVO.setRefundStatus(REFUND_STATUS_NONE);
			orderVO.setRefundReason("\u4ed8\u6b3e\u903e\u6642\uff0c\u7cfb\u7d71\u81ea\u52d5\u53d6\u6d88\uff0c\u672a\u4ed8\u6b3e\u7121\u9700\u9000\u6b3e");
			orderRepo.save(orderVO);
		}

		return overdueOrders.size();
	}

	public int completeEndedPaidOrders() {
		LocalDateTime now = LocalDateTime.now();
		List<ActivityOrderVO> endedOrders = orderRepo.findByOrderStatusAndActivityEnded(ORDER_STATUS_ACTIVE, now);

		for (ActivityOrderVO orderVO : endedOrders) {
			orderVO.setOrderStatus(ORDER_STATUS_COMPLETED);
			if (orderVO.getActivityCompletedAt() == null) {
				orderVO.setActivityCompletedAt(now);
			}
			orderRepo.save(orderVO);
		}

		return endedOrders.size();
	}

	public ActivityOrderVO confirmRefund(Integer activityOrderId, Integer employeeId) {
		ActivityOrderVO orderVO = getOneOrder(activityOrderId);

		if (orderVO == null) {
			return null;
		}

		if (REFUND_STATUS_DONE.equals(orderVO.getRefundStatus())) {
			return orderVO;
		}

		if (!REFUND_STATUS_REQUESTED.equals(orderVO.getRefundStatus())) {
			return orderVO;
		}

		orderVO.setOrderStatus(ORDER_STATUS_COMPLETED);
		orderVO.setRefundStatus(REFUND_STATUS_DONE);
		orderVO.setActivityCompletedAt(LocalDateTime.now());
		orderVO.setEmployeeId(employeeId);
		return orderRepo.save(orderVO);
	}

	public ActivityOrderVO confirmPayout(Integer activityOrderId, Integer employeeId) {
		ActivityOrderVO orderVO = getOneOrder(activityOrderId);

		if (orderVO == null) {
			return null;
		}

		if (!ORDER_STATUS_ACTIVE.equals(orderVO.getOrderStatus())
				&& !ORDER_STATUS_COMPLETED.equals(orderVO.getOrderStatus())) {
			return orderVO;
		}

		orderVO.setOrderStatus(ORDER_STATUS_COMPLETED);
		orderVO.setActivityCompletedAt(LocalDateTime.now());
		orderVO.setPayoutAmount(true);
		orderVO.setEmployeeId(employeeId);
		return orderRepo.save(orderVO);
	}

	private boolean isPaymentExpired(ActivityOrderVO orderVO) {
		return orderVO.getApprovedAt() != null
				&& orderVO.getApprovedAt().plusSeconds(PAYMENT_TIMEOUT_SECONDS).isBefore(LocalDateTime.now());
	}

	private boolean isFreeOrder(ActivityOrderVO orderVO) {
		return orderVO == null || orderVO.getTotalAmount() == null || orderVO.getTotalAmount() <= 0;
	}

	private void applySystemCancellation(ActivityOrderVO orderVO, String reason) {
		boolean paidOrder = ORDER_STATUS_ACTIVE.equals(orderVO.getOrderStatus());
		orderVO.setOrderStatus(ORDER_STATUS_CANCELLED);

		if (paidOrder && !isFreeOrder(orderVO)) {
			orderVO.setRefundStatus(REFUND_STATUS_REQUESTED);
			orderVO.setRefundReason(normalizeReason(reason, "系統取消已付款活動，申請全額退款"));
			return;
		}

		orderVO.setRefundStatus(REFUND_STATUS_NONE);
		if (paidOrder) {
			orderVO.setRefundReason(normalizeReason(reason, "0元訂單取消，無需退款"));
		} else {
			orderVO.setRefundReason(normalizeReason(reason, "訂單尚未付款，系統直接取消"));
		}
	}

	private String normalizeReason(String reason, String fallback) {
		return reason == null || reason.trim().isEmpty() ? fallback : reason.trim();
	}

	private void applyDefaultRefundStatus(ActivityOrderVO orderVO) {
		if (orderVO != null && orderVO.getRefundStatus() == null) {
			orderVO.setRefundStatus(REFUND_STATUS_NONE);
		}
	}

}
