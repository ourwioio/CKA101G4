package com.webond.activity.model;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.webond.activity.repository.ActivityOrderRepository;

@Service
public class ActReviewService {
	
	 @Autowired
	    private ActivityOrderRepository orderRepo;
	 
	 
	 @Transactional
	    public void submitBuyerReview(Integer orderId, Byte rating, String comment) {
	        ActivityOrderVO order = orderRepo.findById(orderId)
	                .orElseThrow(() -> new IllegalArgumentException("找不到該筆訂單"));

	        // 唯讀校驗：檢查同事維護的「訂單狀態」，必須為完成（假設狀態碼是 'COMPLETED'）
	        if (!"COMPLETED".equals(order.getOrderStatus())) {
	            throw new IllegalStateException("訂單尚未完成，無法進行評價！");
	        }

	        // 防重複評價校驗：檢查你負責的欄位是否已有資料
	        if (order.getBuyerRateSeller() != null) {
	            throw new IllegalStateException("該筆訂單您已經評價過了！");
	        }

	        // 精準寫入：只去設定你負責的三個欄位
	        order.setBuyerRateSeller(rating);
	        order.setBuyerReviewComment(comment);
	        order.setBuyerReviewedAt(LocalDateTime.now());

	        // 5. 儲存：配合 @DynamicUpdate，SQL 只會出現 UPDATE activity_order SET buyer_rating=..., buyer_comment=... WHERE order_id=...
	        orderRepo.save(order);
	    }

}
