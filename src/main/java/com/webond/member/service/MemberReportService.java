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
	
	@Autowired
    private MemberDeactivationCoordinator memberDeactivationCoordinator;
	

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
			
			// 2. 防呆機制：若駁回不成立(3)，違規點數一律歸 0
			if (Integer.valueOf(3).equals(reportStatus)) {
				violationPoints = 0;
			}
			
			memberReportVO.setReportStatus(reportStatus);       
			memberReportVO.setAdminNote(adminNote);             
			memberReportVO.setViolationPoints(violationPoints); 
			memberReportVO.setProcessedAt(new Timestamp(System.currentTimeMillis())); 
			
			// 3. 🚀【核心連動】審核通過 (2)，將裁決點數「累加」至被檢舉人的會員累計點數 (reportPoints)
			if (Integer.valueOf(2).equals(reportStatus) && violationPoints != null && violationPoints > 0) {
				MemberVO proxyReported = memberReportVO.getReported(); 
				
				if (proxyReported != null && proxyReported.getMemberId() != null) {
					Optional<MemberVO> memberOpt = memberRepository.findById(proxyReported.getMemberId());
					if (memberOpt.isPresent()) {
						MemberVO actualMember = memberOpt.get();
						
						// 取得目前累計點數 (防呆處理 null)
						Integer currentPoints = actualMember.getReportPoints(); 
						if (currentPoints == null) {
							currentPoints = 0;
						}
						
						// 🎯【重大修正】：這裡是累計點數，要用「加法 (+)」！
						int newTotalPoints = currentPoints + violationPoints;
						actualMember.setReportPoints(newTotalPoints);
						
						// 🚨【自動停權防禦】：當違規總點數達到 5 點時，自動切換帳號狀態為 3 (停權)
						if (newTotalPoints >= 5) {
							actualMember.setAccountStatus((byte) 3);
							System.out.println("🚨 [系統自動停權] 會員 ID: " + actualMember.getMemberId() + " 違規累計已達 " + newTotalPoints + " 點！");
						}
						
						// 🎯 使用 saveAndFlush 強制立刻寫入資料庫
						MemberVO updatedMember = memberRepository.saveAndFlush(actualMember);

						// 此檢舉流程直接操作 MemberRepository，不會經過 MemberServiceLoie，
						// 因此在這裡補上同一個協調入口；未滿 5 點時狀態不是 3，協調器會忽略。
						memberDeactivationCoordinator.handleDisabledMember(updatedMember);
						
						// 重新塞回 Report 物件，維持永續上下文一致
						memberReportVO.setReported(updatedMember);
						
						System.out.println("✅ [成功累計違規點數] 會員 ID: " + updatedMember.getMemberId() 
								+ " 原點數: " + currentPoints 
								+ " + 本次裁決: " + violationPoints 
								+ " => 最新總點數: " + updatedMember.getReportPoints());
					} else {
						System.err.println("❌ [錯誤] 資料庫查無 ID 為 " + proxyReported.getMemberId() + " 的會員！");
					}
				}
			}
			 
			// 4. 儲存檢舉案件狀態
			repository.saveAndFlush(memberReportVO);
			
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
