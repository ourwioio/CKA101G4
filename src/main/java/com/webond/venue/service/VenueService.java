package com.webond.venue.service;

import java.time.LocalDate;
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
	public void addVenueWithImages(VenueVO venueVO, List<byte[]> imageBytesList, int coverIndex) {
	    // 先存場地取得 venueId
	    repository.save(venueVO);
	    repository.flush();

	    // 新增照片
	    for (int i = 0; i < imageBytesList.size(); i++) {
	        VenueImagesVO imageVO = new VenueImagesVO();
	        imageVO.setImages(imageBytesList.get(i));
	        imageVO.setVenueVO(venueVO);
	        imageVO.setCover(i == coverIndex ? (byte) 1 : (byte) 0);
	        venueVO.getVenueImages().add(imageVO);
	    }

	 // 產生未來 15 天的預約時段
	    LocalDate today = LocalDate.now();
	    String openDays = venueVO.getOpenDays();        // 長度7的字串
	    String availableHours = venueVO.getAvailableHours();  // 長度24的字串
	    String closedHours = "2".repeat(24);            // 不可預約的全天字串

	    for (int i = 0; i <= 15; i++) {
	        LocalDate slotDate = today.plusDays(i);
	        int dayOfWeek = slotDate.getDayOfWeek().getValue() - 1;  // 週一=0, 週日=6

	        String slotStatus;
	        if (openDays != null && openDays.length() == 7 && openDays.charAt(dayOfWeek) == '1') {
	            slotStatus = availableHours;  // 開放日用場地設定的時段
	        } else {
	            slotStatus = closedHours;     // 非開放日全部不可預約
	        }

	        VenueSlotVO slotVO = new VenueSlotVO();
	        slotVO.setVenueVO(venueVO);
	        slotVO.setSlotDate(slotDate);
	        slotVO.setSlotStatus(slotStatus);
	        venueVO.getVenueSlots().add(slotVO);
	    }
	    // 照片和時段一起存
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
	
	public List<VenueVO> getAllActive() {
		return repository.findByVenueStatus((byte) 1);
	}
	
	public List<VenueVO> getVenuesByMember(Integer memberId) {
		return repository.findByMember_MemberId(memberId);
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
	
	@Transactional
	public void updateVenueCover(VenueVO venueVO, Integer coverImageId) {
	    VenueVO existingVenue = repository.findById(venueVO.getVenueId()).orElse(null);
	    if (existingVenue == null) return;

	    existingVenue.setVenueName(venueVO.getVenueName());
	    existingVenue.setOpenDays(venueVO.getOpenDays());
	    existingVenue.setAvailableHours(venueVO.getAvailableHours());

	    if (coverImageId != null) {
	        for (VenueImagesVO img : existingVenue.getVenueImages()) {
	            img.setCover(img.getImagesId().equals(coverImageId) ? (byte) 1 : (byte) 0);
	        }
	    }
	    repository.save(existingVenue);
	}

	
	
	

}
