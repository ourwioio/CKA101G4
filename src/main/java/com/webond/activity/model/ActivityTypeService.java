package com.webond.activity.model;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;

@Service // 宣告為 Spring 管理的 Service 元件
@Transactional // 開啟 Spring 事務管理，確保資料庫操作的一致性
public class ActivityTypeService {

	@Autowired
	private ActivityTypeRepository typeRepository;

	/**
	 * 查詢所有活動類型
	 * 
	 * @return 活動類型清單
	 */
	@Transactional(readOnly = true) // 唯讀事務，優化查詢效能
	public List<ActivityTypeVO> getAll() {
		return typeRepository.findAll();
	}

	/**
	 * 根據 ID 查詢特定活動類型
	 * 
	 * @param id 類型編號
	 * @return 活動類型資料
	 */
	@Transactional(readOnly = true)
	public Optional<ActivityTypeVO> getById(Integer id) {
		return typeRepository.findById(id);
	}

	/**
	 * 新增或更新活動類型
	 * 
	 * @param typeVO 活動類型實體
	 * @return 儲存後的活動類型資料
	 */
	public ActivityTypeVO saveType(ActivityTypeVO typeVO) {
		// [商業邏輯邊界檢查]：例如可以檢查類型名稱是否為空或過長
		if (typeVO.getActivityTypeName() == null || typeVO.getActivityTypeName().trim().isEmpty()) {
			throw new IllegalArgumentException("活動類型名稱不能為空！");
		}

		// 消除前後多餘的空白字元
		typeVO.setActivityTypeName(typeVO.getActivityTypeName().trim());

		return typeRepository.save(typeVO);
	}

	/**
	 * 刪除活動類型
	 * 
	 * @param id 類型編號
	 */
	public void deleteType(Integer id) {
		// [商業邏輯驗證]：
		// 這裡未來可以加入額外檢查，例如：「若有活動項目仍綁定此類型，則不允許刪除，並拋出異常」
		if (!typeRepository.existsById(id)) {
			throw new EntityNotFoundException("找不到編號為 " + id + " 的活動類型，無法刪除！");
		}
		typeRepository.deleteById(id);
	}
}