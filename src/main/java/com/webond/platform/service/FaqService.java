package com.webond.platform.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.webond.platform.model.FaqVO;
import com.webond.platform.repository.FaqRepository;

@Service
public class FaqService {

	@Autowired
	FaqRepository repository;

	// ===== 狀態常數 =====
	public static final byte STATUS_DRAFT = 0;
	public static final byte STATUS_PUBLISHED = 1;

	// ===== FAQ 類型常數 =====
	public static final byte TYPE_ACCOUNT = 0; // 帳號問題
	public static final byte TYPE_ORDER = 1; // 訂單問題
	public static final byte TYPE_SERVICE = 2; // 服務問題
	public static final byte TYPE_GROUP_ACTIVITY = 3; // 揪團活動問題
	public static final byte TYPE_VENUE_RENTAL = 4; // 場地租借問題
	public static final byte TYPE_REPORT_DISPUTE = 5; // 檢舉與爭議
	public static final byte TYPE_SYSTEM_OPERATION = 6; // 系統操作問題
	public static final byte TYPE_OTHER = 7; // 其他

	// ===== 新增 =====
	public void addFaq(FaqVO faq) {
		faq.setFaqId(null); // 確保走 INSERT，不會被誤當成更新
		repository.save(faq);
	}

	// ===== 修改 =====
	/**
	 * 修改 FAQ：若該筆已經是發布狀態，強制維持發布，不允許透過修改表單把狀態改回草稿。
	 */
	@Transactional
	public void updateFaq(FaqVO faq) {
		FaqVO existing = getOneFaq(faq.getFaqId());
		if (existing != null && existing.getStatus() == STATUS_PUBLISHED) {
			faq.setStatus(STATUS_PUBLISHED);
		}
		repository.save(faq);
	}

	// ===== 刪除 =====
	public void deleteFaq(Integer faqId) {
		if (repository.existsById(faqId))
			repository.deleteById(faqId);
	}

	// ===== 查詢 =====
	public FaqVO getOneFaq(Integer faqId) {
		Optional<FaqVO> optional = repository.findById(faqId);
		return optional.orElse(null);
	}

	public List<FaqVO> getAll() {
		return repository.findAll();
	}

	public List<FaqVO> getByStatus(Byte status) {
		return repository.findByStatus(status);
	}

	public List<FaqVO> getByFaqType(Byte faqType) {
		return repository.findByFaqType(faqType);
	}

	public List<FaqVO> getByQuestionLike(String keyword) {
		return repository.findByQuestionLike("%" + keyword + "%");
	}

	public List<FaqVO> getByFaqTypeAndStatus(Byte faqType, Byte status) {
		return repository.findByFaqTypeAndStatus(faqType, status);
	}

	// ===== 業務邏輯：發布 =====
	/**
	 * 發布 FAQ，單純把狀態切換為已發布，發布後不可收回為草稿。
	 */
	@Transactional
	public void publish(Integer faqId) {
		FaqVO faq = getOneFaq(faqId);
		if (faq == null)
			return;
		faq.setStatus(STATUS_PUBLISHED);
		repository.save(faq);
	}

	public static final Map<Byte, String> FAQ_TYPE_LABELS;
	static {
		Map<Byte, String> map = new LinkedHashMap<>();
		map.put(TYPE_ACCOUNT, "帳號問題");
		map.put(TYPE_ORDER, "訂單問題");
		map.put(TYPE_SERVICE, "服務問題");
		map.put(TYPE_GROUP_ACTIVITY, "揪團活動問題");
		map.put(TYPE_VENUE_RENTAL, "場地租借問題");
		map.put(TYPE_REPORT_DISPUTE, "檢舉與爭議");
		map.put(TYPE_SYSTEM_OPERATION, "系統操作問題");
		map.put(TYPE_OTHER, "其他");
		FAQ_TYPE_LABELS = map;
	}
}
