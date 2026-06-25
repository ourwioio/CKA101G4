package com.webond.venue.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.webond.venue.model.VenueSlotVO;
import com.webond.venue.repository.VenueSlotRepository;

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
	
	public List<VenueSlotVO> getAll(){
		return repository.findAll();
	}
	
	
}
