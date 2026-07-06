package com.webond.member.service;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.webond.employee.model.EmployeeVO;
import com.webond.member.model.MemberReportVO;
import com.webond.member.model.MemberVO;
import com.webond.member.repository.MemberReportRepository;
import com.webond.member.repository.MemberRepository; 

@Service
public class MemberReportService {

	@Autowired
	private MemberReportRepository repository;
	
	@Autowired
	private MemberRepository memberRepository; 

	// =========================================================================
	// 【業務邏輯 1】會員送出新檢舉案 (前台網頁呼叫)
	// =========================================================================
	public MemberReportVO addMemberReport(Integer reporterId, Integer reportedId, 
			Integer reportCategory, String reportContent, byte[] evidence) {
		MemberReportVO memberReportVO = new MemberReportVO();
		
		MemberVO reporter = new MemberVO();
		reporter.setMemberId(reporterId);
		memberReportVO.setReporter(reporter); 
		
		MemberVO reported = new MemberVO();
		reported.setMemberId(reportedId);
		memberReportVO.setReported(reported); 
		
		memberReportVO.setReportCategory(reportCategory);
		memberReportVO.setReportContent(reportContent);
		memberReportVO.setEvidence(evidence); 
		memberReportVO.setReportStatus(0); // 0: 待處理
		
		memberReportVO.setViolationPoints(0);
		repository.save(memberReportVO);
		return memberReportVO;
	}

	// =========================================================================
	// 【業務邏輯 2】管理員審核檢舉案 (後台網頁呼叫) 
	// =========================================================================
	@Transactional 
	public MemberReportVO jombackProcessReport(Integer reportId, Integer employeeId, 
			Integer reportStatus, String adminNote, Integer violationPoints) {
		
		Optional<MemberReportVO> optional = repository.findById(reportId);
		MemberReportVO memberReportVO = optional.orElse(null);
		
		if (memberReportVO != null) {
			// 1. 綁定經辦員工
			EmployeeVO employee = new EmployeeVO();
			employee.setEmployeeId(employeeId);
			memberReportVO.setEmployee(employee); 
			
			// 2. 防呆機制：若駁回不成立，扣點一律歸 0
			if (Integer.valueOf(2).equals(reportStatus)) {
				violationPoints = 0;
			}
			
			memberReportVO.setReportStatus(reportStatus);       
			memberReportVO.setAdminNote(adminNote);             
			memberReportVO.setViolationPoints(violationPoints); 
			memberReportVO.setProcessedAt(new Timestamp(System.currentTimeMillis())); 
			
			// 3. 🚀【核心連動】審核通過 (1)，扣除被檢舉人的會員點數
			if (Integer.valueOf(1).equals(reportStatus) && violationPoints != null && violationPoints > 0) {
				MemberVO reportedMember = memberReportVO.getReported(); 
				
				if (reportedMember != null && reportedMember.getMemberId() != null) {
					Optional<MemberVO> memberOpt = memberRepository.findById(reportedMember.getMemberId());
					if (memberOpt.isPresent()) {
						MemberVO actualMember = memberOpt.get();
						
						// =============================================================
						// 🔍 【請依據你的 MemberVO 實際欄位名稱微調此處】
						// 範例：若欄位是 memPoints，請改成 actualMember.getMemPoints();
						// =============================================================
						Integer currentPoints = actualMember.getReportPoints(); 
						if (currentPoints == null) currentPoints = 0;
						
						// 執行扣點（防呆最低為 0）
						int newPoints = Math.max(0, currentPoints - violationPoints);
						actualMember.setReportPoints(newPoints);
						// =============================================================
						
						// 儲存更新後的會員資料
						memberRepository.save(actualMember);
						System.out.println("成功對會員 ID " + actualMember.getMemberId() + " 扣除 " + violationPoints + " 點！");
					}
				}
			}
			
			// 4. 儲存檢舉案件狀態
			repository.save(memberReportVO);
			
		} else {
			System.err.println("⚠️ 警告：案號 " + reportId + " 已不存在於資料庫，無法進行審核！");
			return null;
		}
		return memberReportVO;
	}

	// =========================================================================
	// 🟢 【查詢與刪除業務邏輯】
	// =========================================================================
	public MemberReportVO getOneMemberReport(Integer reportId) {
		Optional<MemberReportVO> optional = repository.findById(reportId);
		return optional.orElse(null);
	}

	public List<MemberReportVO> getAll() {
		return repository.findAll();
	}

	public void deleteMemberReport(Integer reportId) {
		if (repository.existsById(reportId)) {
			repository.deleteById(reportId);
		}
	}
}