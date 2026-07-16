package com.webond.venue.scheduler;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.webond.member.model.NotificationVO;
import com.webond.member.service.NotificationService;
import com.webond.venue.model.VenueOrderVO;
import com.webond.venue.repository.VenueOrderRepository;

import jakarta.transaction.Transactional;

@Component
public class OrderCompletionScheduler {

    @Autowired
    private VenueOrderRepository orderRepository;
    
    @Autowired
    private NotificationService notificationService;

    // 每 1 分鐘自動執行一次
    @Scheduled(fixedRate = 60000)
    @Transactional
    public void checkCompletedOrders() {

        LocalDateTime now = LocalDateTime.now();

        // 撈出所有「已付款」狀態的訂單
        List<VenueOrderVO> paidOrders = orderRepository.findByOrderStatus((byte) 1);

        for (VenueOrderVO order : paidOrders) {

            LocalDate bookDate = order.getBookDate();
            LocalTime endAt = order.getEndAt();

            // 組合出這筆預約的時間點
            LocalDateTime reservationEndTime = LocalDateTime.of(bookDate, endAt);

            // 如果預約結束時間已經比現在早，代表這場預約已經結束
            if (reservationEndTime.isBefore(now)) {
                order.setOrderStatus((byte) 3); // 3 = 已完成，可評價
             // 新增通知給買家
        		NotificationVO notificationVO = new NotificationVO();
        		notificationVO.setMember(order.getMember());
        		notificationVO.setTitle("場地評價提醒通知");
        		notificationVO.setContent("您的訂單已完成，請評價場地");
        		notificationVO.setNotificationType((byte) 2);
        		notificationService.addNotification(notificationVO);
                orderRepository.save(order);
            }
        }
    }
}