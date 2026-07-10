package com.webond.venue.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.webond.venue.model.VenueImagesVO;
import com.webond.venue.model.VenueOrderVO;
import com.webond.venue.model.VenueSlotVO;
import com.webond.venue.model.VenueVO;
import com.webond.venue.repository.VenueRepository;
import com.webond.venue.util.HibernateUtil_CompositeQuery_Venue;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

@Service
public class VenueService {

	@Autowired
	VenueRepository repository;

	@PersistenceContext
	private EntityManager entityManager;

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
		String openDays = venueVO.getOpenDays(); // 長度7的字串
		String availableHours = venueVO.getAvailableHours(); // 長度24的字串
		String closedHours = "2".repeat(24); // 不可預約的全天字串

		for (int i = 0; i <= 15; i++) {
			LocalDate slotDate = today.plusDays(i);
			int dayOfWeek = slotDate.getDayOfWeek().getValue() - 1; // 週一=0, 週日=6

			String slotStatus;
			if (openDays != null && openDays.length() == 7 && openDays.charAt(dayOfWeek) == '1') {
				slotStatus = availableHours; // 開放日用場地設定的時段
			} else {
				slotStatus = closedHours; // 非開放日全部不可預約
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
	public List<VenueVO> getAll(Map<String, String[]> map) {
		Session session = entityManager.unwrap(Session.class);
		return HibernateUtil_CompositeQuery_Venue.getAllC(map, session);
	}

	@Transactional
	public void updateVenueCover(VenueVO venueVO, List<byte[]> newImageBytesList, Integer coverImageId,
			Integer coverNewIndex, List<Integer> deleteImageIds) {

		VenueVO existingVenue = repository.findById(venueVO.getVenueId()).orElse(null);
		if (existingVenue == null)
			return;

		// 基本欄位
		existingVenue.setVenueName(venueVO.getVenueName());
		existingVenue.setCapacity(venueVO.getCapacity());
		existingVenue.setHourlyRate(venueVO.getHourlyRate());
		existingVenue.setVenueTypeVO(venueVO.getVenueTypeVO());
		existingVenue.setOpenDays(venueVO.getOpenDays());
		existingVenue.setAvailableHours(venueVO.getAvailableHours());

		// 上下架狀態：只允許切換 0（下架）或 1（上架），審核中(2)等狀態不允許被表單覆蓋
		if (venueVO.getVenueStatus() != null && (venueVO.getVenueStatus() == 0 || venueVO.getVenueStatus() == 1)
				&& existingVenue.getVenueStatus() != 2) {
			existingVenue.setVenueStatus(venueVO.getVenueStatus());
		}

		// 刪除使用者勾選要刪除的既有照片
		if (deleteImageIds != null && !deleteImageIds.isEmpty()) {
			existingVenue.getVenueImages().removeIf(img -> deleteImageIds.contains(img.getImagesId()));
		}

		// 先把剩下照片的封面狀態清空，等下依使用者選擇重新標記
		for (VenueImagesVO img : existingVenue.getVenueImages()) {
			img.setCover((byte) 0);
		}

		// 加入這次新上傳的照片
		List<VenueImagesVO> newImages = new ArrayList<>();
		if (newImageBytesList != null) {
			for (byte[] bytes : newImageBytesList) {
				VenueImagesVO imageVO = new VenueImagesVO();
				imageVO.setImages(bytes);
				imageVO.setVenueVO(existingVenue);
				imageVO.setCover((byte) 0);
				existingVenue.getVenueImages().add(imageVO);
				newImages.add(imageVO);
			}
		}

		// 決定封面：優先看是否選了「這次新上傳」的某張，否則看是否選了「既有照片」
		boolean coverSet = false;
		if (coverNewIndex != null && coverNewIndex >= 0 && coverNewIndex < newImages.size()) {
			newImages.get(coverNewIndex).setCover((byte) 1);
			coverSet = true;
		} else if (coverImageId != null) {
			for (VenueImagesVO img : existingVenue.getVenueImages()) {
				if (img.getImagesId() != null && img.getImagesId().equals(coverImageId)) {
					img.setCover((byte) 1);
					coverSet = true;
				}
			}
		}

		// 如果封面被刪掉、或使用者沒有另外指定封面，剩下的照片裡自動挑一張當封面
		if (!coverSet && !existingVenue.getVenueImages().isEmpty()) {
			existingVenue.getVenueImages().iterator().next().setCover((byte) 1);
		}

		repository.save(existingVenue);
	}

}
