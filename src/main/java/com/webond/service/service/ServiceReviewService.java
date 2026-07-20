package com.webond.service.service;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.webond.member.model.NotificationVO;
import com.webond.member.repository.MemberRepository;
import com.webond.member.service.NotificationService;
import com.webond.service.dto.ServiceReviewDTO;
import com.webond.service.model.ServiceOrderVO;
import com.webond.service.repository.ServiceReviewRepository;

@Service
public class ServiceReviewService {
	
	@Autowired	
	private ServiceReviewRepository serviceReviewRepository;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private MemberRepository memberRepository; 
	
    public ServiceOrderVO getOrderForReview(Integer orderId, Integer currentMemberId) {
		ServiceOrderVO order = serviceReviewRepository.findById(orderId)
				.orElseThrow(() -> new IllegalArgumentException("訂單不存在"));
 
		if (order.getOrderStatus() != 3) {
			throw new IllegalArgumentException("訂單未完成，無法評價");
		}
 
		boolean isBuyer = currentMemberId.equals(order.getBuyerMemberId());
		boolean isSeller = currentMemberId.equals(order.getSellerMemberId());
 
		if (!isBuyer && !isSeller) {
			throw new IllegalArgumentException("無權查看此訂單評價");
		}
 
		return order;
	}
	

	@Transactional
	public void submitReview(Integer orderId, Integer currentMemberId, ServiceReviewDTO dto) {
		if (orderId == null) {
			throw new IllegalArgumentException("訂單編號不可為空");
		}
 
		ServiceOrderVO order = serviceReviewRepository.findById(orderId)
				.orElseThrow(() -> new IllegalArgumentException("訂單不存在"));
 
		if (order.getOrderStatus() != 3) {
			throw new IllegalArgumentException("訂單未完成，無法評價");
		}
 
		if (dto.getRate() == null) {
			throw new IllegalArgumentException("請填寫評價");
		}
 
		LocalDateTime now = LocalDateTime.now();
 
		if (currentMemberId.equals(order.getBuyerMemberId())) {
			if (order.getBuyerReviewedAt() != null) {
				throw new IllegalArgumentException("已評價過此訂單");
			}
			serviceReviewRepository.submitBuyerReview(orderId, dto.getRate(), dto.getComment(), now);
			memberRepository.addServicerRating(order.getSellerMemberId(), java.math.BigDecimal.valueOf(dto.getRate())); // 新增

 
			// 通知賣方：買方已完成評價
			notifyReviewSubmitted(order.getSellerMemberId());
 
			// 若對方（賣方）尚未評價，額外提醒賣方也去評價
			if (order.getSellerReviewedAt() == null) {
				notifyReviewInvite(order.getSellerMemberId());
			}
 
		} else if (currentMemberId.equals(order.getSellerMemberId())) {
			if (order.getSellerReviewedAt() != null) {
				throw new IllegalArgumentException("已評價過此訂單");
			}
			serviceReviewRepository.submitsellerReview(orderId, dto.getRate(), dto.getComment(), now);
			memberRepository.addServiceRating(order.getBuyerMemberId(), java.math.BigDecimal.valueOf(dto.getRate())); // 新增

 
			// 通知買方：賣方已完成評價
			notifyReviewSubmitted(order.getBuyerMemberId());
 
			// 若對方（買方）尚未評價，額外提醒買方也去評價
			if (order.getBuyerReviewedAt() == null) {
				notifyReviewInvite(order.getBuyerMemberId());
			}
 
		} else {
			throw new IllegalArgumentException("無權評價此訂單");
		}
	}
 
	/**
	 * 通知對方：您收到一則新評價
	 */
	private void notifyReviewSubmitted(Integer targetMemberId) {
		NotificationVO notification = new NotificationVO();
		notification.setMember(memberRepository.getReferenceById(targetMemberId));
		notification.setTitle("服務新評價通知");
		notification.setContent("您的服務收到一則新評價，快來查看吧！");
		notification.setNotificationType((byte) 1); // 這裡要填正確的類型代碼
		notificationService.addNotification(notification);
	}
 
	/**
	 * 邀請對方：提醒您也可以進行評價
	 */
	private void notifyReviewInvite(Integer targetMemberId) {
		NotificationVO notification = new NotificationVO();
		notification.setMember(memberRepository.getReferenceById(targetMemberId));
		notification.setTitle("服務評價提醒通知");
		notification.setContent("誠摯邀您為本次服務進行評分與回饋！");
		notification.setNotificationType((byte) 1); // 這裡要填正確的類型代碼
		notificationService.addNotification(notification);
	}

}



