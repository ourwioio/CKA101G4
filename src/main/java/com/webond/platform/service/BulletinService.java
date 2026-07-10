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
	public void addBulletin(BulletinVO bulletinVO) {
		bulletinVO.setBulletinId(null); // 確保走 INSERT，不會被誤當成更新
		repository.save(bulletinVO);
	}
	
	/**
	 * 修改公告：若該筆已經是發布狀態，強制維持發布，
	 * 不允許透過修改表單把狀態改回草稿（即使前端被繞過送出 status=0 也一樣擋下）。
	 */
	@Transactional
	public void updateBulletin(BulletinVO bulletinVO) {
		BulletinVO existingBulletin = getOneBulletin(bulletinVO.getBulletinId());
		if (existingBulletin != null && existingBulletin.getStatus() == STATUS_PUBLISHED) {
			bulletinVO.setStatus(STATUS_PUBLISHED);
			bulletinVO.setPublishDate(existingBulletin.getPublishDate()); // 發布日期也一併鎖定，避免被覆蓋成 null
		}
		repository.save(bulletinVO);
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
	
	public List<BulletinVO> getPublishedOrderByDateDesc() {
	    return repository.findByStatusOrderByPublishDateDesc(STATUS_PUBLISHED);
	}
	
	// ===== 業務邏輯：發布 =====

	/**
	 * 發布公告：第一次發布時寫入 publishDate，
	 * 若已發布過（publishDate 不為 null），不再更動日期。
	 * 發布後不可收回為草稿。
	 */
	@Transactional
	public void publish(Integer bulletinId) {
		BulletinVO bulletinVO = getOneBulletin(bulletinId);
		if (bulletinVO == null) return;
		if (bulletinVO.getPublishDate() == null) {
			bulletinVO.setPublishDate(LocalDate.now());
		}
		bulletinVO.setStatus(STATUS_PUBLISHED);
		repository.save(bulletinVO);
	}
	
	/**
	 * 前台專用：只回傳「已發布」的單筆公告。
	 * 若該筆不存在，或狀態不是已發布（例如還是草稿），一律回傳 null，
	 * 避免前台使用者透過網址猜測 ID 看到未發布的內容。
	 */
	public BulletinVO getPublishedOne(Integer bulletinId) {
	    BulletinVO bulletinVO = getOneBulletin(bulletinId);
	    if (bulletinVO != null && bulletinVO.getStatus() == STATUS_PUBLISHED) {
	        return bulletinVO;
	    }
	    return null;
	}
	
	public List<BulletinVO> getPublishedByDateRange(LocalDate startDate, LocalDate endDate) {
	    return repository.findByStatusAndPublishDateBetween(STATUS_PUBLISHED, startDate, endDate);
	}
}
