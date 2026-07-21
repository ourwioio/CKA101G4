package com.webond.service.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.webond.service.model.ServiceTypeVO;
import com.webond.service.repository.ServiceTypeRepository;

@Service
@Transactional
public class ServiceTypeService {

	private final ServiceTypeRepository serviceTypeRepository;

	public ServiceTypeService(ServiceTypeRepository serviceTypeRepository) {

		this.serviceTypeRepository = serviceTypeRepository;
	}

	// =========================================================
	// 查單一服務類型
	// =========================================================

	@Transactional(readOnly = true)
	public ServiceTypeVO findByPK(Integer serviceTypeId) {

		if (serviceTypeId == null) {
			return null;
		}

		return serviceTypeRepository.findById(serviceTypeId).orElse(null);
	}

	// =========================================================
	// 查全部服務類型
	// =========================================================

	@Transactional(readOnly = true)
	public List<ServiceTypeVO> getAll() {

		return serviceTypeRepository.findAll();
	}

	@Transactional(readOnly = true)
	public List<ServiceTypeVO> search(String keyword) {

		String normalizedKeyword = normalizeNullableText(keyword);

		if (normalizedKeyword == null) {
			return serviceTypeRepository.findAll();
		}

		return serviceTypeRepository.findByTypeNameContainingIgnoreCaseOrDescripContainingIgnoreCase(normalizedKeyword,
				normalizedKeyword);
	}

	// =========================================================
	// 新增服務類型
	//
	// TYPE_MODE、DEFAULT_IMAGE_URL 暫時不使用
	// =========================================================

	public ServiceTypeVO add(String name, String description) {

		validateServiceType(name, description);

		ServiceTypeVO serviceTypeVO = new ServiceTypeVO();

		serviceTypeVO.setTypeName(name.trim());

		serviceTypeVO.setDescrip(normalizeNullableText(description));

		// TYPE_MODE 資料庫欄位為 NOT NULL
		serviceTypeVO.setTypeMode(0);

		// 預設圖片目前未使用
		serviceTypeVO.setImgURL(null);

		return serviceTypeRepository.save(serviceTypeVO);
	}
	// =========================================================
	// 修改服務類型
	//
	// 只修改名稱、描述
	// 不處理 TYPE_MODE、DEFAULT_IMAGE_URL
	// =========================================================

	public ServiceTypeVO update(Integer serviceTypeId, String name, String description) {

		if (serviceTypeId == null) {
			throw new IllegalArgumentException("服務類型編號不可為空");
		}

		validateServiceType(name, description);

		ServiceTypeVO serviceTypeVO = serviceTypeRepository.findById(serviceTypeId)
				.orElseThrow(() -> new IllegalArgumentException("查無此服務類型"));

		serviceTypeVO.setTypeName(name.trim());

		serviceTypeVO.setDescrip(normalizeNullableText(description));

		/*
		 * 修改時不設定 typeMode、imgURL， 避免覆蓋舊資料。
		 */

		return serviceTypeRepository.save(serviceTypeVO);
	}

	// =========================================================
	// 刪除服務類型
	//
	// 若 SERVICE 還有參照這個類型，
	// 資料庫外鍵可能會阻止刪除。
	// =========================================================

	public void delete(Integer serviceTypeId) {

		if (serviceTypeId == null) {
			throw new IllegalArgumentException("服務類型編號不可為空");
		}

		if (!serviceTypeRepository.existsById(serviceTypeId)) {

			throw new IllegalArgumentException("查無此服務類型");
		}

		serviceTypeRepository.deleteById(serviceTypeId);
	}

	// =========================================================
	// 共用驗證
	// =========================================================

	private void validateServiceType(String name, String description) {

		if (name == null || name.trim().isEmpty()) {

			throw new IllegalArgumentException("服務類型名稱不可為空");
		}

		if (name.trim().length() > 50) {

			throw new IllegalArgumentException("服務類型名稱不可超過 50 個字");
		}

		String normalizedDescription = normalizeNullableText(description);

		if (normalizedDescription != null && normalizedDescription.length() > 255) {

			throw new IllegalArgumentException("服務類型描述不可超過 255 個字");
		}
	}

	// =========================================================
	// 共用：空白字串轉成 null
	// =========================================================

	private String normalizeNullableText(String text) {

		if (text == null) {
			return null;
		}

		String normalized = text.trim();

		return normalized.isEmpty() ? null : normalized;
	}
}