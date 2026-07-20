package com.webond.venue.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.webond.member.model.MemberVO;
import com.webond.venue.model.VenueImagesVO;
import com.webond.venue.model.VenueOrderVO;
import com.webond.venue.model.VenueSlotVO;
import com.webond.venue.model.VenueVO;
import com.webond.venue.repository.VenueRepository;
import com.webond.venue.repository.VenueSlotRepository;
import com.webond.venue.util.HibernateUtil_CompositeQuery_Venue;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

@Service
public class VenueService {

	@Autowired
	VenueRepository repository;

	@Autowired
	VenueSlotRepository venueSlotRepository;

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

		for (int i = 0; i < 15; i++) {
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

	public List<VenueVO> getActiveByMember(Integer memberId) {
		return repository.findByVenueStatusAndMember_MemberId((byte) 1, memberId);
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

	// 場地審核通過後，會員申請重新審核以解鎖完整編輯權限；只改狀態，不動其他欄位
	@Transactional
	public void requestReReview(Integer venueId) {
		VenueVO venue = repository.findById(venueId).orElse(null);
		if (venue != null && venue.getVenueStatus() != null && venue.getVenueStatus() != 2) {
			venue.setVenueStatus((byte) 2);
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

		// 審核中(2)才能完整編輯場地基本資料；審核通過後（0下架/1上架）只能改租借費用、開放日、營業時段
		boolean pendingReview = existingVenue.getVenueStatus() != null && existingVenue.getVenueStatus() == 2;

		if (pendingReview) {
			existingVenue.setVenueName(venueVO.getVenueName());
			existingVenue.setAddress(venueVO.getAddress());
			existingVenue.setCapacity(venueVO.getCapacity());
			existingVenue.setVenueTypeVO(venueVO.getVenueTypeVO());
			existingVenue.setVenueDescription(venueVO.getVenueDescription());
		}

		// 租借費用、開放日、營業時段：不論審核狀態皆可修改
		existingVenue.setHourlyRate(venueVO.getHourlyRate());
		existingVenue.setOpenDays(venueVO.getOpenDays());
		existingVenue.setAvailableHours(venueVO.getAvailableHours());

		// 上下架狀態：只允許切換 0（下架）或 1（上架），審核中(2)等狀態不允許被表單覆蓋
		if (venueVO.getVenueStatus() != null && (venueVO.getVenueStatus() == 0 || venueVO.getVenueStatus() == 1)
				&& existingVenue.getVenueStatus() != 2) {
			existingVenue.setVenueStatus(venueVO.getVenueStatus());
		}

		// 照片管理（刪除既有照片、新增、選封面）僅在審核中開放，審核通過後場地照片不能再改
		if (pendingReview) {
			// 刪除使用者勾選要刪除的既有照片
			if (deleteImageIds != null && !deleteImageIds.isEmpty()) {
				existingVenue.getVenueImages().removeIf(img -> deleteImageIds.contains(img.getImagesId()));
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

			// 使用者「真的有指定」新封面時，才清空重新標記；否則保留原本的封面設定，
			// 避免每次存檔沒特別選封面時被隨機換成別張照片
			boolean coverChosen = (coverNewIndex != null && coverNewIndex >= 0 && coverNewIndex < newImages.size())
					|| coverImageId != null;

			if (coverChosen) {
				for (VenueImagesVO img : existingVenue.getVenueImages()) {
					img.setCover((byte) 0);
				}

				boolean coverSet = false;
				if (coverNewIndex != null && coverNewIndex >= 0 && coverNewIndex < newImages.size()) {
					newImages.get(coverNewIndex).setCover((byte) 1);
					coverSet = true;
				} else {
					for (VenueImagesVO img : existingVenue.getVenueImages()) {
						if (img.getImagesId() != null && img.getImagesId().equals(coverImageId)) {
							img.setCover((byte) 1);
							coverSet = true;
						}
					}
				}

				if (!coverSet && !existingVenue.getVenueImages().isEmpty()) {
					existingVenue.getVenueImages().iterator().next().setCover((byte) 1);
				}
			}

			// 保險：如果目前完全沒有任何一張被標記封面（例如原本的封面剛好被刪除、使用者又沒另外指定），
			// 從剩下的照片裡挑一張當封面，避免場地完全沒有封面
			boolean hasCover = existingVenue.getVenueImages().stream()
					.anyMatch(img -> img.getCover() != null && img.getCover() == 1);
			if (!hasCover && !existingVenue.getVenueImages().isEmpty()) {
				existingVenue.getVenueImages().iterator().next().setCover((byte) 1);
			}
		}

		repository.save(existingVenue);

		// 開放日/營業時段可能被改了，未來的時段紀錄要跟著重新計算
		refreshFutureSlots(existingVenue);
	}

	// 場地開放日/營業時段有異動時，重新計算「未來」時段的開放狀態；
	// 已經被預約(1)或正在付款中(3)的小時維持不動，避免把成立的訂單弄壞
	@Transactional
	public void refreshFutureSlots(VenueVO venue) {
		LocalDate today = LocalDate.now();
		List<VenueSlotVO> futureSlots = venueSlotRepository
				.findByVenueVO_VenueIdAndSlotDateGreaterThanEqual(venue.getVenueId(), today);
		String closedHours = "2".repeat(24);

		for (VenueSlotVO slot : futureSlots) {
			int dayOfWeek = slot.getSlotDate().getDayOfWeek().getValue() - 1;
			String newPattern = (venue.getOpenDays().charAt(dayOfWeek) == '1') ? venue.getAvailableHours()
					: closedHours;

			StringBuilder merged = new StringBuilder(slot.getSlotStatus());
			for (int h = 0; h < 24; h++) {
				char current = merged.charAt(h);
				if (current == '1' || current == '3') {
					continue; // 已預約或付款中，保留不動
				}
				merged.setCharAt(h, newPattern.charAt(h));
			}
			slot.setSlotStatus(merged.toString());
			venueSlotRepository.save(slot);
		}
	}

	// 排程器用讓場地都有15天可以讓人預約
	@Transactional
	public void generateNextDaySlotsForAllVenues() {
		List<VenueVO> venues = repository.findByVenueStatus((byte) 1);
		LocalDate targetDate = LocalDate.now().plusDays(14);
		String closedHours = "2".repeat(24);

		for (VenueVO venue : venues) {
			LocalDate maxExistingDate = venueSlotRepository.findMaxSlotDateByVenueId(venue.getVenueId());
			LocalDate startDate = (maxExistingDate == null) ? LocalDate.now() : maxExistingDate.plusDays(1);

			for (LocalDate date = startDate; !date.isAfter(targetDate); date = date.plusDays(1)) {
				int dayOfWeek = date.getDayOfWeek().getValue() - 1;
				String slotStatus = (venue.getOpenDays().charAt(dayOfWeek) == '1') ? venue.getAvailableHours()
						: closedHours;

				VenueSlotVO slotVO = new VenueSlotVO();
				slotVO.setVenueVO(venue);
				slotVO.setSlotDate(date);
				slotVO.setSlotStatus(slotStatus);
				venueSlotRepository.save(slotVO);
			}
		}
	}
	
	public void removeVenue(MemberVO memberVO) {
		List<VenueVO> list = getActiveByMember(memberVO.getMemberId());
		for (VenueVO venueVO : list) {
			venueVO.setVenueStatus((byte) 0);
			updateVenue(venueVO);
		}
	}

	// 排程器用，清除已經過去的舊時段紀錄
	@Transactional
	public void deletePastSlots() {
		venueSlotRepository.deleteBySlotDateBefore(LocalDate.now());
	}

}
