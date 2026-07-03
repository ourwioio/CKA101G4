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

}
