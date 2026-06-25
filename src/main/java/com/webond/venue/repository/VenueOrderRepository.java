package com.webond.venue.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.webond.venue.model.VenueOrderVO;

public interface VenueOrderRepository extends JpaRepository<VenueOrderVO, Integer>{

}
