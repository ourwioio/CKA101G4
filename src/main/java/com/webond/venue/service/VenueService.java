package com.webond.venue.service;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.webond.venue.model.VenueImagesVO;
import com.webond.venue.model.VenueOrderVO;
import com.webond.venue.model.VenueSlotVO;
import com.webond.venue.model.VenueVO;
import com.webond.venue.repository.VenueRepository;

import jakarta.transaction.Transactional;

@Service
public class VenueService {

	@Autowired
	VenueRepository repository;

	@Autowired
	private SessionFactory sessionFactory;

	public void addVenue(VenueVO venueVO) {
		repository.save(venueVO);
	}

	@Transactional
	public void addVenueWithImages(VenueVO venueVO, List<byte[]> imageBytesList) {
			for (byte[] bytes : imageBytesList) {
				VenueImagesVO imageVO = new VenueImagesVO();
				imageVO.setImages(bytes);
				imageVO.setVenueVO(venueVO);  // 場地編號新增到venueVO
				venueVO.getVenueImages().add(imageVO);  // 找照片放進Set集合裡一起新增
		}
		repository.save(venueVO);
	}

	public void updateVenue(VenueVO venueVO) {
		repository.save(venueVO);
	}

	public void deleteVenue(Integer venueId) {
		if (repository.existsById(venueId)) {
			repository.deleteById(venueId);
		}
	}

	public VenueVO getOneVenue(Integer venueId) {
		Optional<VenueVO> optional = repository.findById(venueId);
		return optional.orElse(null);
	}

	public List<VenueVO> getAll() {
		return repository.findAll();
	}

	public Set<VenueImagesVO> getImagesByVenue(Integer venueId) {
		return getOneVenue(venueId).getVenueImages();
	}
	
	public Set<VenueSlotVO> getVenueSlotByVenue(Integer venueId){
		return getOneVenue(venueId).getVenueSlots();
	}
	
	public Set<VenueOrderVO> getVenueOrderByVenue(Integer venueId){
		return getOneVenue(venueId).getVenueOrders();
	}

}
