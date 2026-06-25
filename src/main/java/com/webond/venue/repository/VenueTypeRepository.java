package com.webond.venue.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.webond.venue.model.VenueTypeVO;

public interface VenueTypeRepository extends JpaRepository<VenueTypeVO, Integer>{
	
}
