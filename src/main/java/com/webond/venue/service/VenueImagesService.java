package com.webond.venue.service;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.webond.venue.model.VenueImagesVO;
import com.webond.venue.repository.VenueImagesRepository;

@Service
public class VenueImagesService {

	@Autowired
	VenueImagesRepository venueImagesRepository;
	
	@Autowired
	SessionFactory sessionFactory;
	
	public VenueImagesVO getOneImage(Integer imagesId) {
	    return venueImagesRepository.findById(imagesId).orElse(null);
	}
	
	public void deleteImage(Integer imagesId) {
	    venueImagesRepository.deleteById(imagesId);
	}
	
	
}
