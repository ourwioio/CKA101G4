package com.webond.member.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.webond.employee.model.EmployeeVO;
import com.webond.member.model.MemberVO;
import com.webond.member.repository.MemberRepository;

@Service
public class MemberServiceLoie {

	@Autowired
	private MemberRepository repository;

	// =========================================================================
	// 🔐 1. 登入功能：從資料庫查詢登入的使用者 (完全對齊老師的 UserService 邏輯)
	// =========================================================================
	public MemberVO findByEmail(String email) {
		Optional<MemberVO> optional = repository.findByEmail(email);
		return optional.orElse(null); // 如果值存在就回傳，否則回傳 null
	}

	// =========================================================================
	// 🙋‍♂️ 2. 註冊同時 KYC 功能：初始化規格書要求的欄位與預設值
	// =========================================================================
	@Transactional
	public MemberVO registerMember(String email, String passwordHash, String nickname, 
			Byte gender, String phone, String realName, String idNumber, 
			byte[] idImage, byte[] faceImage, String bankCode, String bankAccount) {
		
		MemberVO member = new MemberVO();
		
		// 填入前端傳來的必要欄位
		member.setEmail(email);
		member.setPasswordHash(passwordHash); 
		member.setNickname(nickname);
		member.setGender(gender);
		member.setPhone(phone);
		member.setRealName(realName);
		member.setIdNumber(idNumber);
		member.setIdImage(idImage);
		member.setFaceImage(faceImage);
		member.setBankCode(bankCode);
		member.setBankAccount(bankAccount);
		
		// 🛠️ 依據規格書，自動初始化預設值
		member.setAccountStatus((byte) 0);   // 0：未驗證
		member.setKycStatus((byte) 0);       // 0：審核中
		member.setReportPoints(0);           // 🎯 檢舉累計點數初始為 0
		member.setCreatedAt(LocalDate.now()); // 建立時間
		member.setSubmittedAt(LocalDateTime.now()); // 送審時間
		
		// 初始化四種評價總分與次數（規格書標記：預設 0）
		member.setServiceRateSum(BigDecimal.ZERO);
		member.setServiceRateCount(0);
		member.setServicerRateSum(BigDecimal.ZERO);
		member.setServicerRateCount(0);
		member.setActRateSum(BigDecimal.ZERO);
		member.setActRateCount(0);
		member.setHoldactRateSum(BigDecimal.ZERO);
		member.setHoldactRateCount(0);
		
		return repository.save(member);
	}

	// =========================================================================
	// 💻 3. 後台管理：管理員審核 KYC 實名認證 (保留你原有的嚴謹審核邏輯)
	// =========================================================================
	@Transactional
	public MemberVO reviewKycStatus(Integer memberId, Integer employeeId, Byte kycStatus) {
		
		Optional<MemberVO> optional = repository.findById(memberId);
		if (optional.isPresent()) {
			MemberVO member = optional.get();
			
			// 1. 綁定審核員工的外殼物件 (EMPLOYEE_ID 外來鍵)
			EmployeeVO employee = new EmployeeVO();
			employee.setEmployeeId(employeeId);
			member.setEmployee(employee);
			
			// 2. 更新審核狀態與時間
			member.setKycStatus(kycStatus);             // 1：審核通過, 2：審核失敗
			member.setReviewedAt(LocalDateTime.now());   // 填入審核時間
			
			// 3. 連動邏輯：如果 KYC 審核通過，帳號狀態自動同步開通為「1：正常」
			if (kycStatus == 1) {
				member.setAccountStatus((byte) 1); 
			}
			
			return repository.save(member);
		}
		return null;
	}

	// =========================================================================
	// 🔍 後台輔助查詢：列出所有會員大總表
	// =========================================================================
	public List<MemberVO> getAllMembers() {
		return repository.findAll();
	}
	
	// 🔍 後台輔助查詢：取得單一會員詳細資料 (審核細頁、圖片顯示用)
	public MemberVO getOneMember(Integer memberId) {
		return repository.findById(memberId).orElse(null);
	}

	// =========================================================================
	// 🎯 接收 MemberVO 的註冊存檔實作 (完美對齊 Controller doRegister 呼叫)
	// =========================================================================
	@Transactional
	public void registerMember(MemberVO memberVO) {
		// 初始化規格書要求的評價預設值
		memberVO.setReportPoints(0);
		memberVO.setServiceRateSum(BigDecimal.ZERO);
		memberVO.setServiceRateCount(0);
		memberVO.setServicerRateSum(BigDecimal.ZERO);
		memberVO.setServicerRateCount(0);
		memberVO.setActRateSum(BigDecimal.ZERO);
		memberVO.setActRateCount(0);
		memberVO.setHoldactRateSum(BigDecimal.ZERO);
		memberVO.setHoldactRateCount(0);
		
		// 真正呼叫 repository 送進資料庫
		repository.save(memberVO);
	}

	// =========================================================================
	// 🎯 補齊後台功能：依據狀態碼撈取會員清單 (用於前端 kycApproval.html 的待審核表格)
	// =========================================================================
	public List<MemberVO> getMembersByStatus(byte b) {
		// 使用 Java Stream 語法，從所有會員中過濾出 AccountStatus 符合條件的會員
		return repository.findAll().stream()
				.filter(m -> m.getAccountStatus() != null && m.getAccountStatus() == b)
				.toList();
	}

	// =========================================================================
	// 🎯 補齊後台功能：更新會員資料 (連動「滿 5 點自動停權」核心商業邏輯)
	// =========================================================================
	@Transactional
	public void updateMember(MemberVO memberVO) {
		if (memberVO != null && memberVO.getMemberId() != null) {
			
			// 🎯 核心自動化防禦：檢查這名會員當前的檢舉點數 reportPoints
			int currentPoints = memberVO.getReportPoints() != null ? memberVO.getReportPoints() : 0;
			
			// 只要點數累計大於等於 5 點，且目前不處於已停權狀態，後端直接強制將狀態改為 3
			if (currentPoints >= 5 && (memberVO.getAccountStatus() == null || memberVO.getAccountStatus() != 3)) {
				memberVO.setAccountStatus((byte) 3); // 3 = 已停權
				System.out.println("==== 🚨 [系統自動化防禦] 會員 ID: " + memberVO.getMemberId() + " 檢舉點數已達 " + currentPoints + " 點，自動執行停權！ ====");
			}
			
			repository.save(memberVO);
		}
	}

	// =========================================================================
	// 🚨 補齊後台功能：被檢舉時點數累加 API（供其他模組/檢舉功能呼叫）
	// =========================================================================
	@Transactional
	public void reportMember(Integer memberId) {
		Optional<MemberVO> optional = repository.findById(memberId);
		if (optional.isPresent()) {
			MemberVO member = optional.get();
			int currentPoints = member.getReportPoints() != null ? member.getReportPoints() : 0;
			
			// 點數累加 1
			member.setReportPoints(currentPoints + 1);
			
			// 🎯 呼叫內部的 updateMember，讓它自動進去觸發「滿 5 點就變更 accountStatus=3」的檢查機制
			this.updateMember(member);
		}
	}

	// =========================================================================
	// 🔍 補齊後台功能：關鍵字模糊搜尋全體會員（支援 Email、真實姓名、手機）
	// =========================================================================
	public List<MemberVO> searchMembers(String keyword) {
		if (keyword == null || keyword.trim().isEmpty()) {
			return repository.findAll();
		}
		
		String lowerKeyword = keyword.toLowerCase().trim();
		
		// 🎯 最快速直覺的做法：利用 Stream 在記憶體中進行多欄位模糊比對過濾
		return repository.findAll().stream()
				.filter(m -> 
					(m.getEmail() != null && m.getEmail().toLowerCase().contains(lowerKeyword)) ||
					(m.getRealName() != null && m.getRealName().toLowerCase().contains(lowerKeyword)) ||
					(m.getPhone() != null && m.getPhone().contains(lowerKeyword))
				)
				.toList();
	}
}