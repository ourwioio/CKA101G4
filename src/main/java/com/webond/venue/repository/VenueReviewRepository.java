package com.webond.venue.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.webond.venue.model.VenueReviewVO;

public interface VenueReviewRepository extends JpaRepository<VenueReviewVO, Integer>{
	
    // ===== JpaRepository 原有方法 =====
	
    // save(entity)        → 新增或更新
    // findAll()           → 查詢全部
    // findById(id)        → 依主鍵查單筆
    // deleteById(id)      → 依主鍵刪除
    // count()             → 計算總筆數
    // existsById(id)      → 判斷是否存在

    // ===== 自訂查詢方法 =====
	
    // 依審核狀態查詢
    List<VenueReviewVO> findByReviewStatus(Byte reviewStatus);

    // 依場地編號查詢（一個場地可能有多筆審核紀錄）
    List<VenueReviewVO> findByVenueId(Integer venueId);

    // 依負責員工查詢
    List<VenueReviewVO> findByEmployeeId(Integer employeeId);
    
 // 取該場地最新一筆審核紀錄
    VenueReviewVO findTopByVenueIdOrderByVenueReviewIdDesc(Integer venueId);
    
}
