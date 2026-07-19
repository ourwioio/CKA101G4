package com.webond.venue.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.webond.venue.dto.VenueOrderQueryDTO;
import com.webond.venue.model.VenueOrderVO;
import com.webond.venue.model.VenueVO;
import com.webond.venue.repository.VenueOrderRepository;
import com.webond.venue.repository.VenueRepository;
import com.webond.venue.util.VenueOrderSpecification;

import jakarta.transaction.Transactional;

@Service
public class VenueOrderService {

	@Autowired
	VenueOrderRepository repository;

	@Autowired
	VenueSlotService venueSlotService;
	
	@Autowired
	VenueRepository venueRepository;

	// 修改你原本的 addVenueOrder 方法：
	@Transactional
	public void addVenueOrder(VenueOrderVO venueOrder) {
		// 1. 先請 SlotService 去把時段字串鎖定並改成 '3'。如果被別人佔用了，這裡會直接噴錯誤彈開
		venueSlotService.reserveSlotForPayment(venueOrder.getVenueSlotId(), venueOrder.getStartAt().getHour(),
				venueOrder.getEndAt().getHour());

		// 2. 時段順利佔用成功後，設定這筆訂單的預設狀態
		venueOrder.setOrderStatus((byte) 0); // 0 = 待付款 / 預約保留中
		venueOrder.setCreatedAt(LocalDateTime.now()); // 填入建立時間，計時器才知道有沒有超過5分鐘

		// 3. 儲存訂單
		repository.save(venueOrder);
	}

	@Transactional
	public void updateVenueOrder(VenueOrderVO venueOrder) {
		repository.save(venueOrder);
	}

	@Transactional
	public void deleteVenueOrder(Integer venueOrderId) {
		if (repository.existsById(venueOrderId))
			repository.deleteById(venueOrderId);
	}

	public VenueOrderVO getOneVenueOrder(Integer venueOrderId) {
		Optional<VenueOrderVO> optional = repository.findById(venueOrderId);
		return optional.orElse(null);
	}

	public List<VenueOrderVO> getAll() {
		return repository.findAll();
	}

	public List<VenueOrderVO> getVenuesByMember(Integer memberId) {
		return repository.findByMember_MemberIdOrderByVenueOrderIdDesc(memberId);
	}

	public List<VenueOrderVO> search(VenueOrderQueryDTO params) {
		return repository.findAll(VenueOrderSpecification.search(params));
	}
	
	public List<VenueOrderVO> getMyAllReservations(Integer memberId){
		List<VenueOrderVO> list = new ArrayList<>(repository.findPaidOrdersByVenueOwner(memberId, (byte) 1));
		return list;
	}
	public List<VenueOrderVO> getMyAllCompletedBookings(Integer memberId){
		return repository.findPaidOrdersByVenueOwner(memberId, (byte) 3);
	}
	
	public List<VenueOrderVO> getAllRefundStatus(){
		return repository.findByRefundStatus((byte) 0);
	}
	
	public List<VenueOrderVO> getAllPayoutAmount(){
		return repository.findByPayoutAmount((byte) 0); 
	}
	
	/** 提交評價：只能評一次，同步更新場地的總分與評分人數 */
	@Transactional
	public void submitReview(Integer venueOrderId, Integer memberId, Integer rating, String comment) {

		VenueOrderVO order = repository.findById(venueOrderId)
				.orElseThrow(() -> new RuntimeException("訂單不存在"));

		if (order.getOrderStatus() != 3) {
			throw new RuntimeException("此訂單尚未完成，無法評價");
		}

		if (!order.getMember().getMemberId().equals(memberId)) {
			throw new RuntimeException("無權限評價此訂單");
		}

		if (order.getVenueRating() != null) {
			throw new RuntimeException("此訂單已經評價過了");
		}

		order.setVenueRating(rating);
		order.setVenueComment(comment);
		repository.save(order);

		VenueVO venue = order.getVenueVO();
		int currentStars = venue.getRatingStars() == null ? 0 : venue.getRatingStars();
		int currentCount = venue.getRatingCount() == null ? 0 : venue.getRatingCount();

		venue.setRatingStars(currentStars + rating);
		venue.setRatingcount(currentCount + 1);
		venueRepository.save(venue);
	}

	public List<VenueOrderVO> getReviewsByVenue(Integer venueId) {
	    return repository.findByVenueVO_VenueIdAndVenueRatingIsNotNullOrderByHandledAtDesc(venueId);
	}
	
}
