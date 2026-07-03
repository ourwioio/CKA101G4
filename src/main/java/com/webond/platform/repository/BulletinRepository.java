package com.webond.platform.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import com.webond.platform.model.BulletinVO;

public interface BulletinRepository extends JpaRepository<BulletinVO, Integer> {

    // ===== JpaRepository 原有方法 =====
	
    // save(entity)        → 新增或更新
    // findAll()           → 查詢全部
    // findById(id)        → 依主鍵查單筆
    // deleteById(id)      → 依主鍵刪除
    // count()             → 計算總筆數
    // existsById(id)      → 判斷是否存在

    // ===== 自訂查詢方法 =====

    // 依狀態查詢（0：草稿 / 1：已發布）
    @Query("FROM BulletinVO WHERE status = ?1 ORDER BY bulletinId DESC")
    List<BulletinVO> findByStatus(Byte status);

    // 依標題關鍵字模糊查詢
    @Query("FROM BulletinVO WHERE title LIKE ?1 ORDER BY bulletinId DESC")
    List<BulletinVO> findByTitleLike(String keyword);

    // 依標籤關鍵字模糊查詢
    @Query("FROM BulletinVO WHERE tags LIKE ?1 ORDER BY bulletinId DESC")
    List<BulletinVO> findByTagsLike(String keyword);

    // 依發布日期區間查詢
    @Query("FROM BulletinVO WHERE publishDate BETWEEN ?1 AND ?2 ORDER BY publishDate DESC")
    List<BulletinVO> findByPublishDateBetween(LocalDate startDate, LocalDate endDate);

    // 複合條件查詢（狀態 + 標題關鍵字）
    @Query("FROM BulletinVO WHERE status = ?1 AND title LIKE ?2 ORDER BY bulletinId DESC")
    List<BulletinVO> findByStatusAndTitleLike(Byte status, String keyword);
    
    // 查詢方法依發布日期排序
    List<BulletinVO> findByStatusOrderByPublishDateDesc(Byte status);
    
    // 查詢方法（已發布 + 日期區間）
    @Query("FROM BulletinVO WHERE status = ?1 AND publishDate BETWEEN ?2 AND ?3 ORDER BY publishDate DESC")
    List<BulletinVO> findByStatusAndPublishDateBetween(Byte status, LocalDate startDate, LocalDate endDate);
}