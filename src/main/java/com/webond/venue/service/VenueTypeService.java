package com.webond.venue.service;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.webond.venue.model.VenueTypeVO;
import com.webond.venue.model.VenueVO;
import com.webond.venue.repository.VenueTypeRepository;

@Service
public class VenueTypeService {

	@Autowired
	VenueTypeRepository repository;

	public void addVenueType(VenueTypeVO venueTypeVO) {
		repository.save(venueTypeVO);
	}

	public void updateVenueType(VenueTypeVO venueTypeVO) {
		repository.save(venueTypeVO);
	}

	public void deleteVenueType(Integer venueTypeId) {
		if (repository.existsById(venueTypeId))
			repository.deleteById(venueTypeId);
	}

	public VenueTypeVO getOneVenueType(Integer venueTypeId) {
		Optional<VenueTypeVO> optional = repository.findById(venueTypeId);
		return optional.orElse(null);
	}

	public List<VenueTypeVO> getAll() {
		return repository.findAll();
	}
	
	public Set<VenueVO> getVenueByVenueType(Integer venueTypeId){
		return getOneVenueType(venueTypeId).getVenues();
	}
	
}
