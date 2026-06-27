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
import com.webond.venue.repository.VenueImagesRepository;
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
			imageVO.setVenueVO(venueVO); // 場地編號新增到venueVO
			venueVO.getVenueImages().add(imageVO); // 找照片放進Set集合裡一起新增
		}
		repository.save(venueVO);
	}
	
	@Transactional
	public void updateVenueWithImages(VenueVO venueVO, List<byte[]> imageBytesList) {
	    // 從資料庫取出現有場地（保留舊照片、createdAt、venueStatus等）
	    VenueVO existingVenue = repository.findById(venueVO.getVenueId()).orElse(null);
	    
	    // 更新基本欄位
	    existingVenue.setVenueName(venueVO.getVenueName());
	    existingVenue.setAddress(venueVO.getAddress());
	    existingVenue.setCapacity(venueVO.getCapacity());
	    existingVenue.setHourlyRate(venueVO.getHourlyRate());
	    existingVenue.setOpenDays(venueVO.getOpenDays());
	    existingVenue.setAvailableHours(venueVO.getAvailableHours());
	    existingVenue.setVenueTypeVO(venueVO.getVenueTypeVO());

	    // 如果有新照片才加進去
	    for (byte[] bytes : imageBytesList) {
	        VenueImagesVO imageVO = new VenueImagesVO();
	        imageVO.setImages(bytes);
	        imageVO.setVenueVO(existingVenue);
	        existingVenue.getVenueImages().add(imageVO);
	    }

	    repository.save(existingVenue);
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

	public Set<VenueSlotVO> getVenueSlotByVenue(Integer venueId) {
		return getOneVenue(venueId).getVenueSlots();
	}

	public Set<VenueOrderVO> getVenueOrderByVenue(Integer venueId) {
		return getOneVenue(venueId).getVenueOrders();
	}
	
	@Transactional
	public void toggleVenueStatus(Integer venueId) {
	    VenueVO venue = repository.findById(venueId).orElse(null);
	    if (venue != null) {
	        // 0 → 1，1 → 0
	        byte newStatus = (venue.getVenueStatus() == 0) ? (byte) 1 : (byte) 0;
	        venue.setVenueStatus(newStatus);
	        repository.save(venue);
	    }
	}
	
	

}
