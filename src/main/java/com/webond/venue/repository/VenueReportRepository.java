package com.webond.venue.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.webond.venue.model.VenueReviewVO;

public interface VenueReportRepository extends JpaRepository<VenueReviewVO, Integer>{

}
