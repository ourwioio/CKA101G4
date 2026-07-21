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
     * 1. 賣家確認是否逾期
     * 2. 買家付款是否逾期
     * 3. 已成立訂單的服務時間是否已結束
     */
    @Scheduled(fixedDelay = 1000)
    public void processServiceOrders() {

        int sellerExpiredCount =
                serviceOrderService
                        .expireOverdueSellerConfirmOrders();

        int paymentExpiredCount =
                serviceOrderService
                        .expireOverduePaymentOrders();

        int completedCount =
                serviceOrderService
                        .completeFinishedOrders();

        if (sellerExpiredCount > 0
                || paymentExpiredCount > 0
                || completedCount > 0) {

//            System.out.println(
//                    "服務訂單排程處理完成："
//                    + "賣家確認逾期 "
//                    + sellerExpiredCount
//                    + " 筆，買家付款逾期 "
//                    + paymentExpiredCount
//                    + " 筆，自動完成 "
//                    + completedCount
//                    + " 筆"
//            );
        }
        
        
    }
}