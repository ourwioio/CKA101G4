package com.webond.platform.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import com.webond.platform.model.BulletinVO;

public interface BulletinRepository extends JpaRepository<BulletinVO, Integer> {

    // 刪除公告
    @Transactional
    @Modifying
    @Query(value = "DELETE FROM BULLETIN WHERE BULLETIN_ID = ?1", nativeQuery = true)
    void deleteByBulletinId(int bulletinId);

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
}