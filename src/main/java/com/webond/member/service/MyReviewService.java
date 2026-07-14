package com.webond.member.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.webond.activity.model.ActivityOrderVO;
import com.webond.activity.repository.ActivityOrderRepository;
import com.webond.service.dto.MemberReviewDTO;
import com.webond.service.model.ServiceOrderVO;
import com.webond.service.repository.ServiceOrderRepository;
import com.webond.venue.model.VenueOrderVO;
import com.webond.venue.repository.VenueOrderRepository;

@Service
public class MyReviewService {
	
	@Autowired
	ServiceOrderRepository orderRepo;

	@Autowired
	ActivityOrderRepository activityOrderRepo;

	@Autowired
	VenueOrderRepository venueOrderRepo;

	@Transactional(readOnly = true)
	public List<MemberReviewDTO> getReviewsByMemberId(Integer memberId) {

	    List<MemberReviewDTO> result = new ArrayList<>();

	    // 服務：賣家收到的評論
	    for (ServiceOrderVO o : orderRepo.findBySellerMemberIdAndBuyerReviewCommentIsNotNull(memberId)) {
	        result.add(new MemberReviewDTO(o.getServiceOrderId(), o.getBuyerReviewComment(),
	                o.getBuyerRateSeller(), o.getBuyerReviewedAt(), "AS_SERVICE_SELLER"));
	    }

	    // 服務：買家收到的評論
	    for (ServiceOrderVO o : orderRepo.findByBuyerMemberIdAndSellerReviewCommentIsNotNull(memberId)) {
	        result.add(new MemberReviewDTO(o.getServiceOrderId(), o.getSellerReviewComment(),
	                o.getSellerRateBuyer(), o.getSellerReviewedAt(), "AS_SERVICE_BUYER"));
	    }

	    // 活動：主辦方收到的評論
	    for (ActivityOrderVO o : activityOrderRepo.findHostReviews(memberId)) {
	        result.add(new MemberReviewDTO(o.getActivityOrderId(), o.getBuyerReviewComment(),
	                o.getBuyerRateSeller(), o.getBuyerReviewedAt(), "AS_ACTIVITY_HOST"));
	    }

	    // 場地：場地主收到的評論
	    for (VenueOrderVO o : venueOrderRepo.findVenueOwnerReviews(memberId)) {
	        result.add(new MemberReviewDTO(o.getVenueOrderId(), o.getVenueComment(),
	                o.getVenueRating() != null ? o.getVenueRating().byteValue() : null,
	                o.getCreatedAt(), "AS_VENUE_OWNER"));
	    }

	    result.sort(Comparator.comparing(MemberReviewDTO::getReviewedAt).reversed());
	    return result;
	}

}
