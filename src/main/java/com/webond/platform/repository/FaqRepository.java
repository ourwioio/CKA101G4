package com.webond.platform.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import com.webond.platform.model.FaqVO;

public interface FaqRepository extends JpaRepository<FaqVO, Integer> {

    // 刪除 FAQ
    @Transactional
    @Modifying
    @Query(value = "DELETE FROM FAQ WHERE FAQ_ID = ?1", nativeQuery = true)
    void deleteByFaqId(int faqId);

    // 依狀態查詢（0：草稿 / 1：已發布）
    @Query("FROM FaqVO WHERE status = ?1 ORDER BY faqId DESC")
    List<FaqVO> findByStatus(Byte status);

    // 依 FAQ 類型查詢
    @Query("FROM FaqVO WHERE faqType = ?1 ORDER BY faqId DESC")
    List<FaqVO> findByFaqType(Byte faqType);

    // 依問題關鍵字模糊查詢
    @Query("FROM FaqVO WHERE question LIKE ?1 ORDER BY faqId DESC")
    List<FaqVO> findByQuestionLike(String keyword);

    // 複合條件查詢（FAQ 類型 + 狀態）
    @Query("FROM FaqVO WHERE faqType = ?1 AND status = ?2 ORDER BY faqId DESC")
    List<FaqVO> findByFaqTypeAndStatus(Byte faqType, Byte status);
}