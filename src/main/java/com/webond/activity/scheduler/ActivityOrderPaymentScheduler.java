package com.webond.activity.scheduler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.webond.activity.model.ActivityOrderService;

@Component
public class ActivityOrderPaymentScheduler {

	@Autowired
	private ActivityOrderService activityOrderSvc;

	@Scheduled(fixedDelay = 5000)
	public void expireOverduePendingPaymentOrders() {
		activityOrderSvc.expireOverduePendingPaymentOrders();
	}
}
