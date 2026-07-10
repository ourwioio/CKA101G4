package com.webond.platform.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.webond.platform.model.PlatformSpecificationVO;
import com.webond.platform.repository.PlatformSpecificationRepository;

@Service
public class PlatformSpecificationService {

	@Autowired
	PlatformSpecificationRepository repository;
	
	// ===== 狀態常數 =====
	public static final byte STATUS_DRAFT = 0;
	public static final byte STATUS_PUBLISHED = 1;
	
	// ===== 規範類型常數 =====
	public static final byte TYPE_ACCOUNT_MEMBERSHIP = 0;   // 帳號與會員規範
	public static final byte TYPE_PAYMENT_REFUND = 1;       // 付款與退款規範
	public static final byte TYPE_SERVICE = 2;              // 服務規範
	public static final byte TYPE_GROUP_ACTIVITY = 3;       // 揪團活動規範
	public static final byte TYPE_VENUE = 4;                // 場地規範
	public static final byte TYPE_REPORT_DISPUTE = 5;       // 檢舉與爭議處理規範
	public static final byte TYPE_SAFETY_PRIVACY = 6;       // 安全與隱私規範
	public static final byte TYPE_OTHER = 7;                // 其他
	
	// ===== 新增 =====
	public void addSpec(PlatformSpecificationVO platformSpecificationVO) {
		platformSpecificationVO.setSpecId(null); // 確保走 INSERT，不會被誤當成更新
		repository.save(platformSpecificationVO);
	}
	
	// ===== 修改 =====
	/**
	 * 修改規範：若該筆已經是發布狀態，強制維持發布，
	 * 不允許透過修改表單把狀態改回草稿。
	 */
	@Transactional
	public void updateSpec(PlatformSpecificationVO platformSpecificationVO) {
		PlatformSpecificationVO existingPlatformSpecification = getOneSpec(platformSpecificationVO.getSpecId());
		if (existingPlatformSpecification != null && existingPlatformSpecification.getStatus() == STATUS_PUBLISHED) {
			platformSpecificationVO.setStatus(STATUS_PUBLISHED);
		}
		repository.save(platformSpecificationVO);
	}
	
	// ===== 刪除 =====
	public void deleteSpec(Integer specId) {
		if (repository.existsById(specId))
			repository.deleteById(specId);
	}

	// ===== 查詢 =====
	public PlatformSpecificationVO getOneSpec(Integer specId) {
		Optional<PlatformSpecificationVO> optional = repository.findById(specId);
		return optional.orElse(null);
	}

	public List<PlatformSpecificationVO> getAll() {
		return repository.findAll();
	}

	public List<PlatformSpecificationVO> getByStatus(Byte status) {
		return repository.findByStatus(status);
	}

	public List<PlatformSpecificationVO> getBySpecType(Byte specType) {
		return repository.findBySpecType(specType);
	}

	public List<PlatformSpecificationVO> getByTitleLike(String keyword) {
		return repository.findByTitleLike("%" + keyword + "%");
	}

	public List<PlatformSpecificationVO> getBySpecTypeAndStatus(Byte specType, Byte status) {
		return repository.findBySpecTypeAndStatus(specType, status);
	}

	// ===== 業務邏輯：發布 =====
	/**
	 * 發布規範。此 Entity 沒有發布日期欄位，
	 * 單純把狀態切換為已發布，發布後不可收回為草稿。
	 */
	@Transactional
	public void publish(Integer specId) {
		PlatformSpecificationVO platformSpecificationVO = getOneSpec(specId);
		if (platformSpecificationVO == null) return;
		platformSpecificationVO.setStatus(STATUS_PUBLISHED);
		repository.save(platformSpecificationVO);
	}
	
	public static final Map<Byte, String> SPEC_TYPE_LABELS;
	static {
	    Map<Byte, String> map = new LinkedHashMap<>();
	    map.put(TYPE_ACCOUNT_MEMBERSHIP, "帳號與會員規範");
	    map.put(TYPE_PAYMENT_REFUND, "付款與退款規範");
	    map.put(TYPE_SERVICE, "服務規範");
	    map.put(TYPE_GROUP_ACTIVITY, "揪團活動規範");
	    map.put(TYPE_VENUE, "場地規範");
	    map.put(TYPE_REPORT_DISPUTE, "檢舉與爭議處理規範");
	    map.put(TYPE_SAFETY_PRIVACY, "安全與隱私規範");
	    map.put(TYPE_OTHER, "其他");
	    SPEC_TYPE_LABELS = map;
	}
}
