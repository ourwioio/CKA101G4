package com.webond.venue.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.webond.venue.dto.VenueOrderQueryDTO;
import com.webond.venue.model.VenueOrderVO;
import com.webond.venue.repository.VenueOrderRepository;
import com.webond.venue.util.VenueOrderSpecification;

import jakarta.transaction.Transactional;

@Service
public class VenueOrderService {

	@Autowired
	VenueOrderRepository repository;

	@Autowired
	VenueSlotService venueSlotService;

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
		return repository.findByMember_MemberId(memberId);
	}

	public List<VenueOrderVO> search(VenueOrderQueryDTO params) {
		return repository.findAll(VenueOrderSpecification.search(params));
	}
	
	public List<VenueOrderVO> getMyAllReservations(Integer memberId){
		return repository.findPaidOrdersByVenueOwner(memberId, (byte) 1);
	}
	public List<VenueOrderVO> getMyAllCompletedBookings(Integer memberId){
		return repository.findPaidOrdersByVenueOwner(memberId, (byte) 3);
	}

	
}
