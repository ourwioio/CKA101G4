package com.webond.activity.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.webond.activity.repository.ActivityOrderRepository;
import com.webond.activity.repository.ActivityRepository;
import com.webond.member.repository.MemberRepository;

@Service
public class ActReviewService {
	
	 @Autowired
	    private ActivityOrderRepository orderRepo;
	 

	    @Autowired
	    private ActivityRepository activityRepo;

	    @Autowired
	    private MemberRepository memberRepository;
	 
	 
	 
	    @Transactional
	    public void saveBuyerReview(ActivityOrderVO order, Byte rating, String comment) {
	        order.setBuyerRateSeller(rating);
	        order.setBuyerReviewComment(comment);
	        order.setBuyerReviewedAt(LocalDateTime.now());
	        orderRepo.save(order);

	        // 新增：查出主辦方，更新其「舉辦活動評分」統計
	        ActivityVO activity = activityRepo.getReferenceById(order.getActivityId());
	        memberRepository.addHoldactRating(activity.getMemberId(), BigDecimal.valueOf(rating));
	    }
	    


}
