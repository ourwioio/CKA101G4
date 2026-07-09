package com.webond.venue.scheduler;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.webond.venue.model.VenueOrderVO;
import com.webond.venue.repository.VenueOrderRepository;
import com.webond.venue.service.VenueSlotService;

import jakarta.transaction.Transactional;

@Component
public class OrderTimeoutScheduler {

    @Autowired
    private VenueOrderRepository orderRepository;

    @Autowired
    private VenueSlotService venueSlotService;

    // 每 30 秒自動執行一次
    @Scheduled(fixedRate = 30000)
    @Transactional
    public void checkTimeoutOrders() {
        // 撈出 5 分鐘前建立、且狀態還是 0 (待付款) 的訂單
        LocalDateTime timeoutLimit = LocalDateTime.now().minusMinutes(5);
        List<VenueOrderVO> expiredOrders = orderRepository.findByOrderStatusAndCreatedAtBefore((byte) 0, timeoutLimit);

        for (VenueOrderVO order : expiredOrders) {
            // 1. 將訂單變更為已取消 (2)
            order.setOrderStatus((byte) 2);
            orderRepository.save(order);

            // 2. 將時段字串從 '3' 退回 '0'
            venueSlotService.releaseTimeoutSlot(
                    order.getVenueSlotId(), 
                    order.getStartAt().getHour(), 
                    order.getEndAt().getHour()
            );
        }
    }
}