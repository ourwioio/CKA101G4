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
	    public void saveBuyerReview(ActivityOrderVO order, Byte rating, String comment) {
	        order.setBuyerRateSeller(rating);
	        
	        order.setBuyerReviewComment(comment);
	        
	        order.setBuyerReviewedAt(LocalDateTime.now());
	        
	        orderRepo.save(order);
	    }
	    


}
