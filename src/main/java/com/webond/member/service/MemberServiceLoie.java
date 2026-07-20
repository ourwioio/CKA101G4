package com.webond.member.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.webond.activity.model.ActivityService;
import com.webond.employee.model.EmployeeVO;
import com.webond.member.model.MemberVO;
import com.webond.member.repository.MemberRepository;
import com.webond.service.service.ServiceService;
import com.webond.venue.service.VenueService;

@Service
public class MemberServiceLoie {

	@Autowired
	private MemberRepository repository;

	@Autowired
	private VenueService venueService; // 確保你的變數名稱與實際注入的 Service 一致
	
	@Autowired
	private ActivityService activityService;
	
	@Autowired
	private ServiceService serviceService;
	// =========================================================================
	// 🔐 1. 登入功能：從資料庫查詢登入的使用者
	// =========================================================================
	public MemberVO findByEmail(String email) {
		if (email == null || email.trim().isEmpty()) {
			return null;
		}
		Optional<MemberVO> optional = repository.findByEmail(email.trim());
		return optional.orElse(null);
	}

	// =========================================================================
	// 🙋‍♂️ 2. 註冊同時 KYC 功能：初始化規格書要求的欄位與預設值
	// =========================================================================
	@Transactional
	public MemberVO registerMember(String email, String passwordHash, String nickname, 
			Byte gender, String phone, String realName, String idNumber, 
			byte[] idImage, byte[] faceImage, String bankCode, String bankAccount) {
		
		MemberVO member = new MemberVO();
		
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
		
		// 🛠️ 初始化預設值
		// 狀態說明：0：未驗證, 1：正常, 2：註銷, 3：停權, 4：限制參加活動
		member.setAccountStatus((byte) 0);   // 預設為 0 (未驗證/待審核)
		member.setKycStatus((byte) 0);       // 0：審核中
		member.setReportPoints(0);           // 累計點數初始為 0
		member.setCreatedAt(LocalDate.now()); 
		member.setSubmittedAt(LocalDateTime.now());
		
		// 初始化四種評價總分與次數
		initMemberRatings(member);
		
		return repository.save(member);
	}

	// =========================================================================
	// 🎯 接收 MemberVO 的註冊存檔實作 ( Controller doRegister 專用)
	// =========================================================================
	@Transactional
	public void registerMember(MemberVO memberVO) {
		if (memberVO == null) return;
		
		// 狀態說明：0：未驗證, 1：正常, 2：註銷, 3：停權, 4：限制參加活動
		memberVO.setAccountStatus((byte) 0);
		memberVO.setKycStatus((byte) 0);
		memberVO.setReportPoints(0);
		memberVO.setCreatedAt(LocalDate.now());
		memberVO.setSubmittedAt(LocalDateTime.now());
		
		initMemberRatings(memberVO);
		
		repository.save(memberVO);
	}

	// =========================================================================
	// 💻 3. 後台管理：管理員審核 KYC 實名認證 (連動帳號狀態)
	// =========================================================================
	@Transactional
	public MemberVO reviewKycStatus(Integer memberId, Integer employeeId, Byte kycStatus) {
		
		Optional<MemberVO> optional = repository.findById(memberId);
		if (optional.isPresent()) {
			MemberVO member = optional.get();
			
			// 1. 綁定經辦員工
			EmployeeVO employee = new EmployeeVO();
			employee.setEmployeeId(employeeId);
			member.setEmployee(employee);
			
			// 2. 更新審核狀態與時間
			member.setKycStatus(kycStatus);             // 1：審核通過, 2：審核失敗
			member.setReviewedAt(LocalDateTime.now());   
			
			// 3. 連動邏輯：
			// kycStatus == 1 (通過) -> accountStatus 改為 1 (正常)
			// kycStatus == 2 (不通過) -> accountStatus 保持或改為 0 (未驗證)
			if (Byte.valueOf((byte) 1).equals(kycStatus)) {
				member.setAccountStatus((byte) 1); 
			} else if (Byte.valueOf((byte) 2).equals(kycStatus)) {
				member.setAccountStatus((byte) 0);
			}
			
			return repository.saveAndFlush(member);
		}
		return null;
	}

	// =========================================================================
	// 🔍 後台輔助查詢
	// =========================================================================
	public List<MemberVO> getAllMembers() {
		return repository.findAll();
	}
	
	public MemberVO getOneMember(Integer memberId) {
		if (memberId == null) return null;
		return repository.findById(memberId).orElse(null);
	}

	// =========================================================================
	// 🎯 補齊後台功能：依據帳戶狀態碼撈取會員清單
	// =========================================================================
	public List<MemberVO> getMembersByStatus(byte status) {
		return repository.findByAccountStatus(status);
	}

	// =========================================================================
	// 🎯 補齊後台功能：安全更新會員資料 (滿 5 點自動停權 status = 3)
	// =========================================================================
	@Transactional
	public void updateMember(MemberVO memberVO) {
		if (memberVO == null || memberVO.getMemberId() == null) {
			return;
		}

		Optional<MemberVO> opt = repository.findById(memberVO.getMemberId());
		if (opt.isPresent()) {
			MemberVO dbMember = opt.get();
			
			if (memberVO.getReportPoints() != null) {
				dbMember.setReportPoints(memberVO.getReportPoints());
			}
			
			if (memberVO.getAccountStatus() != null) {
				dbMember.setAccountStatus(memberVO.getAccountStatus());
			}
			
			// 🚨 核心自動化防禦：檢查累計違規點數 (>= 5 點自動將狀態改為 3：已停權)
			int currentPoints = dbMember.getReportPoints() != null ? dbMember.getReportPoints() : 0;
			
			if (currentPoints >= 5 && (dbMember.getAccountStatus() == null || dbMember.getAccountStatus() != 3)) {
				dbMember.setAccountStatus((byte) 3); 
				// --- 🟢 這裡呼叫你已經寫好的方法 ---
			    venueService.removeVenue(dbMember); 
			    activityService.cancelActivitiesAndOrdersByDisabledMember(dbMember.getMemberId());
			    serviceService.disableAllActiveServicesByMemberId(dbMember.getMemberId());
			    // -------------------------------
				System.out.println("==== 🚨 [系統自動化防禦] 會員 ID: " + dbMember.getMemberId() + " 檢舉點數已達 " + currentPoints + " 點，自動執行停權 (Status=3)！ ====");
			}

		}
	}

	// =========================================================================
	// 🚨 補齊後台功能：被檢舉時點數累加 API
	// =========================================================================
	@Transactional
	public void reportMember(Integer memberId) {
		if (memberId == null) return;

		Optional<MemberVO> optional = repository.findById(memberId);
		if (optional.isPresent()) {
			MemberVO member = optional.get();
			int currentPoints = member.getReportPoints() != null ? member.getReportPoints() : 0;
			
			member.setReportPoints(currentPoints + 1);
			this.updateMember(member);
		}
	}

	// =========================================================================
	// 🔍 補齊後台功能：關鍵字模糊搜尋全體會員
	// =========================================================================
	public List<MemberVO> searchMembers(String keyword) {
		if (keyword == null || keyword.trim().isEmpty()) {
			return repository.findAll();
		}
		
		String kw = keyword.trim();
		return repository.findByEmailContainingOrRealNameContainingOrPhoneContaining(kw, kw, kw);
	}

	// =========================================================================
	// 🛠️ 私有輔助工具：評價預設值初始化
	// =========================================================================
	private void initMemberRatings(MemberVO member) {
		member.setServiceRateSum(BigDecimal.ZERO);
		member.setServiceRateCount(0);
		member.setServicerRateSum(BigDecimal.ZERO);
		member.setServicerRateCount(0);
		member.setActRateSum(BigDecimal.ZERO);
		member.setActRateCount(0);
		member.setHoldactRateSum(BigDecimal.ZERO);
		member.setHoldactRateCount(0);
	}
}
