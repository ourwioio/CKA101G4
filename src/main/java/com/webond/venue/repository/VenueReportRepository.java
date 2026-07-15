package com.webond.venue.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.webond.venue.model.VenueReportVO;

public interface VenueReportRepository extends JpaRepository<VenueReportVO, Integer>{

	   // ===== JpaRepository 原有方法 =====
    // save(entity)        → 新增或更新
    // findAll()           → 查詢全部
    // findById(id)        → 依主鍵查單筆
    // deleteById(id)      → 依主鍵刪除
    // count()             → 計算總筆數
    // existsById(id)      → 判斷是否存在

    // ===== 自訂查詢方法 =====

    // 依處理狀態查詢（0：審核中 / 1：通過 / 2：未通過）
    @Query("FROM VenueReportVO WHERE reportStatus = ?1 ORDER BY serReportTime DESC")
    List<VenueReportVO> findByReportStatus(Byte reportStatus);

    // 依場地訂單編號查詢
    @Query("FROM VenueReportVO WHERE venueOrderId = ?1 ORDER BY serReportTime DESC")
    List<VenueReportVO> findByVenueOrderId(Integer venueOrderId);

    // 依多筆訂單編號查詢（用於批次判斷是否已檢舉過）
    List<VenueReportVO> findByVenueOrderIdIn(List<Integer> venueOrderIds);

    // 依處理員工編號查詢
    @Query("FROM VenueReportVO WHERE employeeId = ?1 ORDER BY serReportTime DESC")
    List<VenueReportVO> findByEmployeeId(Integer employeeId);

    // 依檢舉內容關鍵字模糊查詢
    @Query("FROM VenueReportVO WHERE serReportCom LIKE ?1 ORDER BY serReportTime DESC")
    List<VenueReportVO> findBySerReportComLike(String keyword);

    // 依檢舉時間區間查詢
    @Query("FROM VenueReportVO WHERE serReportTime BETWEEN ?1 AND ?2 ORDER BY serReportTime DESC")
    List<VenueReportVO> findBySerReportTimeBetween(LocalDateTime startTime, LocalDateTime endTime);

    // 複合條件查詢（狀態 + 檢舉內容關鍵字）
    @Query("FROM VenueReportVO WHERE reportStatus = ?1 AND serReportCom LIKE ?2 ORDER BY serReportTime DESC")
    List<VenueReportVO> findByReportStatusAndSerReportComLike(Byte reportStatus, String keyword);

    // 全部查詢並依檢舉時間新到舊排序
    List<VenueReportVO> findAllByOrderBySerReportTimeDesc();
}
