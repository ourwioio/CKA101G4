package com.webond.venue.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.webond.venue.model.VenueOrderVO;
import com.webond.venue.model.VenueVO;
import com.webond.venue.repository.VenueOrderRepository;

@Service
public class VenueOrderService {

	@Autowired
	VenueOrderRepository repository;
	
	public void addVenueOrder(VenueOrderVO venueOrder) {
		repository.save(venueOrder);
	}
	
	public void updateVenueOrder(VenueOrderVO venueOrder) {
		repository.save(venueOrder);
	}
	
	public void deleteVenueOrder(Integer venueOrderId) {
		if (repository.existsById(venueOrderId))
			repository.deleteById(venueOrderId);
	}
	
	public VenueOrderVO getOneVenueOrder(Integer venueOrderId) {
		Optional<VenueOrderVO> optional = repository.findById(venueOrderId);
		return optional.orElse(null);
	}
	
	public List<VenueOrderVO> getAll(){
		return repository.findAll();
	}
	
	public List<VenueOrderVO> getVenuesByMember(Integer memberId) {
		return repository.findByMember_MemberId(memberId);
	}
	
	
}
