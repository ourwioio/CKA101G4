package com.webond.venue.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.webond.venue.model.VenueSlotVO;

public interface VenueSlotRepository extends JpaRepository<VenueSlotVO, Integer>{

}
