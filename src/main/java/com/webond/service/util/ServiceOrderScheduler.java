package com.webond.service.util;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.webond.service.service.ServiceOrderService;

@Component
public class ServiceOrderScheduler {

    private final ServiceOrderService serviceOrderService;

    public ServiceOrderScheduler(
            ServiceOrderService serviceOrderService) {

        this.serviceOrderService = serviceOrderService;
    }

    /**
     * 每秒檢查一次：
     * 1. 賣家是否超過60秒未確認
     * 2. 買家是否超過30秒未付款
     */
    @Scheduled(fixedDelay = 1000)
    public void expireOverdueOrders() {

        int sellerExpiredCount =
                serviceOrderService
                        .expireOverdueSellerConfirmOrders();

        int paymentExpiredCount =
                serviceOrderService
                        .expireOverduePaymentOrders();

        if (sellerExpiredCount > 0
                || paymentExpiredCount > 0) {

            System.out.println(
                    "服務訂單逾期處理完成："
                    + "賣家確認逾期 "
                    + sellerExpiredCount
                    + " 筆，買家付款逾期 "
                    + paymentExpiredCount
                    + " 筆"
            );
        }
    }
}