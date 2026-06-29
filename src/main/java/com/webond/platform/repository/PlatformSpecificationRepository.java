package com.webond.platform.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import com.webond.platform.model.PlatformSpecificationVO;

public interface PlatformSpecificationRepository extends JpaRepository<PlatformSpecificationVO, Integer> {

    // ===== JpaRepository 原有方法 =====
	
    // save(entity)        → 新增或更新
    // findAll()           → 查詢全部
    // findById(id)        → 依主鍵查單筆
    // deleteById(id)      → 依主鍵刪除
    // count()             → 計算總筆數
    // existsById(id)      → 判斷是否存在

    // ===== 自訂查詢方法 =====

    // 依狀態查詢（0：草稿 / 1：已發布）
    @Query("FROM PlatformSpecificationVO WHERE status = ?1 ORDER BY specId DESC")
    List<PlatformSpecificationVO> findByStatus(Byte status);

    // 依規範類型查詢
    @Query("FROM PlatformSpecificationVO WHERE specType = ?1 ORDER BY specId DESC")
    List<PlatformSpecificationVO> findBySpecType(Byte specType);

    // 依標題關鍵字模糊查詢
    @Query("FROM PlatformSpecificationVO WHERE title LIKE ?1 ORDER BY specId DESC")
    List<PlatformSpecificationVO> findByTitleLike(String keyword);

    // 複合條件查詢（規範類型 + 狀態）
    @Query("FROM PlatformSpecificationVO WHERE specType = ?1 AND status = ?2 ORDER BY specId DESC")
    List<PlatformSpecificationVO> findBySpecTypeAndStatus(Byte specType, Byte status);
}