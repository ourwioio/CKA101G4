package com.webond.venue.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.webond.venue.model.VenueSlotVO;
import com.webond.venue.repository.VenueSlotRepository;

import jakarta.transaction.Transactional;

@Service
public class VenueSlotService {

	@Autowired
	VenueSlotRepository repository;

	public void addVenueSlot(VenueSlotVO venueSlotVO) {
		repository.save(venueSlotVO);
	}

	public void updateVenueSlot(VenueSlotVO venueSlotVO) {
		repository.save(venueSlotVO);
	}

	public void deleteVenueSlot(Integer venueSlotId) {
		if (repository.existsById(venueSlotId))
			repository.deleteById(venueSlotId);
	}

	public VenueSlotVO getOneVenueSlot(Integer venueSlotId) {
		Optional<VenueSlotVO> optional = repository.findById(venueSlotId);
		return optional.orElse(null);
	}

	public List<VenueSlotVO> getAll() {
		return repository.findAll();
	}

	@Transactional
	public void updateSlotStatus(Integer venueSlotId, int startHour, int endHour) {
		VenueSlotVO slotVO = repository.findById(venueSlotId).orElse(null);
		if (slotVO == null)
			return;

		StringBuilder sb = new StringBuilder(slotVO.getSlotStatus());
		for (int h = startHour; h < endHour; h++) {
			sb.setCharAt(h, '1'); // 1 = 已預約
		}
		slotVO.setSlotStatus(sb.toString());
		repository.save(slotVO);
	}

	@Transactional
	public void releaseSlotStatus(Integer venueSlotId, int startHour, int endHour) {
		VenueSlotVO slotVO = repository.findById(venueSlotId).orElse(null);
		if (slotVO == null)
			return;

		StringBuilder sb = new StringBuilder(slotVO.getSlotStatus());
		for (int h = startHour; h < endHour; h++) {
			if (sb.charAt(h) == '1') {
				sb.setCharAt(h, '0');
			}
		}
		slotVO.setSlotStatus(sb.toString());
		repository.save(slotVO);
	}

	// 在你的 VenueSlotService 中加上這三個方法：

	@Transactional
	public void reserveSlotForPayment(Integer venueSlotId, int startHour, int endHour) {
		// 1. 用悲觀鎖撈資料
		VenueSlotVO slotVO = repository.findByIdForUpdate(venueSlotId)
				.orElseThrow(() -> new RuntimeException("預約失敗：該日期場地時段不存在"));

		StringBuilder sb = new StringBuilder(slotVO.getSlotStatus()); // 注意：你的欄位叫 slotStatus

		// 2. 檢查這段時間是不是都是 '0' (可預約)
		for (int h = startHour; h < endHour; h++) {
			if (sb.charAt(h) != '0') {
				throw new RuntimeException("預約失敗：您選擇的時段 [" + h + ":00] 已被預約或正在付款中！");
			}
		}

		// 3. 檢查通過，全部暫時改成 '3' (付款中)
		for (int h = startHour; h < endHour; h++) {
			sb.setCharAt(h, '3');
		}

		slotVO.setSlotStatus(sb.toString());
		repository.save(slotVO); // 方法結束，自動放鎖
	}

	@Transactional
	public void confirmSlotPayment(Integer venueSlotId, int startHour, int endHour) {
		
		if (venueSlotId == null) return;
		
		VenueSlotVO slotVO = repository.findById(venueSlotId).orElse(null);
		if (slotVO == null)
			return;

		StringBuilder sb = new StringBuilder(slotVO.getSlotStatus());
		for (int h = startHour; h < endHour; h++) {
			if (sb.charAt(h) == '3') {
				sb.setCharAt(h, '1'); // 扶正為 1 (已預約)
			}
		}
		slotVO.setSlotStatus(sb.toString());
		repository.save(slotVO);
	}

	@Transactional
	public void releaseTimeoutSlot(Integer venueSlotId, int startHour, int endHour) {
		
		if (venueSlotId == null) {
	        return; 
	    }
		
		VenueSlotVO slotVO = repository.findById(venueSlotId).orElse(null);
		if (slotVO == null)
			return;

		StringBuilder sb = new StringBuilder(slotVO.getSlotStatus());
		for (int h = startHour; h < endHour; h++) {
			if (sb.charAt(h) == '3') {
				sb.setCharAt(h, '0'); // 還原為 0 (可預約)
			}
		}
		slotVO.setSlotStatus(sb.toString());
		repository.save(slotVO);
	}

}
