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
	
	public void updateBulletin(BulletinVO bulletin) {
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
	
	// ===== 業務邏輯：發布 / 收回 =====

	/**
	 * 發布公告：第一次發布時寫入 publishDate，
	 * 若已發布過（publishDate 不為 null），不再更動日期。
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

	/** 收回為草稿，publishDate 依規則不清空，只切換 status。 */
	@Transactional
	public void unpublish(Integer bulletinId) {
		BulletinVO bulletin = getOneBulletin(bulletinId);
		if (bulletin == null) return;
		bulletin.setStatus(STATUS_DRAFT);
		repository.save(bulletin);
	}
	
}
