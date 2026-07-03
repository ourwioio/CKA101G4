package com.webond.member.service;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.webond.employee.model.EmployeeVO;
import com.webond.member.model.MemberReportVO;
import com.webond.member.model.MemberVO;
import com.webond.member.repository.MemberReportRepository; // 🟢 請確認你的 Repository 實際路徑
// 💡 以下這兩個 VO 是你在 JPA 外鍵關聯中需要用到的，請根據實際專案路徑 import 進來
// import com.webond.member.model.MemberVO; 
// import com.webond.employee.model.EmployeeVO; 

@Service // 🎯 關鍵：加上這個註解，Spring Boot 就會自動管理這個大腦
public class MemberReportService {

	// 🤝 改用 Spring Boot 自動僱用懂 JPA 的 Repository 大總管，不用再自己 new 囉！
	@Autowired
	private MemberReportRepository repository;

	// =========================================================================
	// 【業務邏輯 1】會員送出新檢舉案 (前台網頁呼叫)
	// =========================================================================
	public MemberReportVO addMemberReport(Integer reporterId, Integer reportedId, 
			Integer reportCategory, String reportContent, byte[] evidence) {

		// 1. 準備一個 (VO)，把前端傳過來的資料裝進去
		MemberReportVO memberReportVO = new MemberReportVO();
		
		// 🎯 因應 JPA 的「外鍵物件化」設計，我們要建立外殼物件並設定 ID
		MemberVO reporter = new MemberVO();
		reporter.setMemberId(reporterId);
		memberReportVO.setReporter(reporter); // 對接檢舉人
		
		MemberVO reported = new MemberVO();
		reported.setMemberId(reportedId);
		memberReportVO.setReported(reported); // 對接被檢舉人
		
		memberReportVO.setReportCategory(reportCategory);
		memberReportVO.setReportContent(reportContent);
		memberReportVO.setEvidence(evidence); // 🟢 配合你之前設定的 byte[] 圖片欄位
		
		// 🛠️ 貼心幫忙：自動補上初始狀態與時間
		memberReportVO.setReportStatus(0); // 0 代表「待處理」
		memberReportVO.setCreatedAt(new Timestamp(System.currentTimeMillis())); // 自動生成當前檢舉時間

		// 2. 指派 Repository 把這一籃資料放進 MySQL 資料庫
		repository.save(memberReportVO);

		// 3. 回傳這個裝滿資料的物件
		return memberReportVO;
	}

	// =========================================================================
	// 【業務邏輯 2】管理員審核檢舉案 (後台網頁呼叫) 
	// =========================================================================
	public MemberReportVO jombackProcessReport(Integer reportId, Integer employeeId, 
			Integer reportStatus, String adminNote, Integer violationPoints) {

		// 1. 因為是更新「既有的舊案件」，用 JPA 的 findById 搭配 Optional 把這筆檢舉案從資料庫撈出來
		Optional<MemberReportVO> optional = repository.findById(reportId);
		MemberReportVO memberReportVO = optional.orElse(null);

		// 💡 確定有這筆案子才動手改
		if (memberReportVO != null) {
			// 2. 把管理員在後台審核、勾選、寫下的全新結果覆蓋進去
			
			// 🎯 員工外鍵對接：建立員工外殼物件
			EmployeeVO employee = new EmployeeVO();
			employee.setEmployeeId(employeeId);
			memberReportVO.setEmployee(employee); 
			
			memberReportVO.setReportStatus(reportStatus);       // 狀態改為你傳進來的審核結果
			memberReportVO.setAdminNote(adminNote);             // 審核備註
			memberReportVO.setViolationPoints(violationPoints); // 判定扣幾點
			memberReportVO.setProcessedAt(new Timestamp(System.currentTimeMillis())); // 補上結案審核時間

			// 3. 呼叫 save() 執行 UPDATE 寫回資料庫
			repository.save(memberReportVO);
		} else {
			// 如果不幸被別人先刪掉了，列印出警告，並直接回傳 null
			System.err.println("⚠️ 警告：案號 " + reportId + " 已不存在於資料庫，無法進行審核！");
			return null;
		}

		return memberReportVO;
	}

	// =========================================================================
	// 【業務邏輯 3】看單一案件的詳細內容 (後台點擊某一列時呼叫)
	// =========================================================================
	public MemberReportVO getOneMemberReport(Integer reportId) {
		Optional<MemberReportVO> optional = repository.findById(reportId);
		return optional.orElse(null); // 完全遵循老師範例的寫法，安全回傳
	}

	// =========================================================================
	// 【業務邏輯 4】列出所有檢舉案 (後台管理首頁表格呼叫)
	// =========================================================================
	public List<MemberReportVO> getAll() {
		return repository.findAll();
	}

	// =========================================================================
	// 【業務邏輯 5】刪除檢舉案 (特殊特殊功能或測試清除資料用)
	// =========================================================================
	public void deleteMemberReport(Integer reportId) {
		if (repository.existsById(reportId)) {
			repository.deleteById(reportId);
		}
	}
}