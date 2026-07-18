package com.webond.activity.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.webond.activity.repository.ActivityOrderRepository;

@ExtendWith(MockitoExtension.class)
class ActivityOrderServiceTest {

	@Mock
	private ActivityOrderRepository orderRepo;

	@InjectMocks
	private ActivityOrderService orderService;

	@Test
	void cancelOrdersByActivity_shouldCancelUnfinishedOrdersAndRequestRefundForPaidOrder() {
		ActivityOrderVO paidOrder = order(1, 0, 500);
		ActivityOrderVO freeOrder = order(1, 0, 0);
		ActivityOrderVO pendingReviewOrder = order(1, 2, 500);
		ActivityOrderVO pendingPaymentOrder = order(1, 3, 500);
		List<ActivityOrderVO> orders = List.of(
				paidOrder, freeOrder, pendingReviewOrder, pendingPaymentOrder);
		when(orderRepo.findByActivityIdAndOrderStatusIn(org.mockito.ArgumentMatchers.eq(1), anyList()))
				.thenReturn(orders);

		int cancelledCount = orderService.cancelOrdersByActivity(1, "會員停權或註銷");

		assertEquals(4, cancelledCount);
		assertEquals((byte) 1, paidOrder.getOrderStatus());
		assertEquals((byte) 1, paidOrder.getRefundStatus());
		assertEquals((byte) 1, freeOrder.getOrderStatus());
		assertEquals((byte) 0, freeOrder.getRefundStatus());
		assertEquals((byte) 1, pendingReviewOrder.getOrderStatus());
		assertEquals((byte) 0, pendingReviewOrder.getRefundStatus());
		assertEquals((byte) 1, pendingPaymentOrder.getOrderStatus());
		assertEquals((byte) 0, pendingPaymentOrder.getRefundStatus());
		assertTrue(paidOrder.getRefundReason().contains("會員停權或註銷"));
		verify(orderRepo).saveAll(orders);
	}

	private ActivityOrderVO order(Integer activityId, int orderStatus, int totalAmount) {
		ActivityOrderVO orderVO = new ActivityOrderVO();
		orderVO.setActivityId(activityId);
		orderVO.setOrderStatus((byte) orderStatus);
		orderVO.setTotalAmount(totalAmount);
		return orderVO;
	}
}
