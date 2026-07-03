package com.webond.platform.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.webond.platform.model.BulletinVO;
import com.webond.platform.repository.BulletinRepository;

@Service
public class BulletinService {

	@Autowired
	BulletinRepository repository;
	
	// 狀態常數，避免 magic number 散落各處
	public static final byte STATUS_DRAFT = 0;
	public static final byte STATUS_PUBLISHED = 1;
	
	// ===== 新增 / 修改 =====
	public void addBulletin(BulletinVO bulletin) {
		bulletin.setBulletinId(null); // 確保走 INSERT，不會被誤當成更新
		repository.save(bulletin);
	}
	
	/**
	 * 修改公告：若該筆已經是發布狀態，強制維持發布，
	 * 不允許透過修改表單把狀態改回草稿（即使前端被繞過送出 status=0 也一樣擋下）。
	 */
	@Transactional
	public void updateBulletin(BulletinVO bulletin) {
		BulletinVO existing = getOneBulletin(bulletin.getBulletinId());
		if (existing != null && existing.getStatus() == STATUS_PUBLISHED) {
			bulletin.setStatus(STATUS_PUBLISHED);
			bulletin.setPublishDate(existing.getPublishDate()); // 發布日期也一併鎖定，避免被覆蓋成 null
		}
		repository.save(bulletin);
	}
	
	// ===== 刪除 =====
	public void deleteBulletin(Integer bulletinId) {
		if (repository.existsById(bulletinId))
			repository.deleteById(bulletinId);
	}
	
	// ===== 查詢 =====
	public BulletinVO getOneBulletin(Integer bulletinId) {
		Optional<BulletinVO> optional = repository.findById(bulletinId);
		return optional.orElse(null);
	}
	
	public List<BulletinVO> getAll(){
		return repository.findAll();
	}
	
	public List<BulletinVO> getByStatus(Byte status) {
		return repository.findByStatus(status);
	}
	
	public List<BulletinVO> getByTitleLike(String keyword){
		return repository.findByTitleLike("%" + keyword + "%");
	}
	
	public List<BulletinVO> getByTagsLike(String keyword){
		return repository.findByTagsLike("%" + keyword + "%");
	}
	
	public List<BulletinVO> getByPublishDateBetween(LocalDate startDate, LocalDate endDate){
		return repository.findByPublishDateBetween(startDate, endDate);
	}
	
	public List<BulletinVO> getByStatusAndTitleLike(Byte status, String keyword) {
		return repository.findByStatusAndTitleLike(status, "%" + keyword + "%");
	}
	
	// ===== 業務邏輯：發布 =====

	/**
	 * 發布公告：第一次發布時寫入 publishDate，
	 * 若已發布過（publishDate 不為 null），不再更動日期。
	 * 發布後不可收回為草稿。
	 */
	@Transactional
	public void publish(Integer bulletinId) {
		BulletinVO bulletin = getOneBulletin(bulletinId);
		if (bulletin == null) return;
		if (bulletin.getPublishDate() == null) {
			bulletin.setPublishDate(LocalDate.now());
		}
		bulletin.setStatus(STATUS_PUBLISHED);
		repository.save(bulletin);
	}
	
}
