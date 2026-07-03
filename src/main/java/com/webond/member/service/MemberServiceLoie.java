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
		member.setReportPoints(0);           // 檢舉累計點數初始為 0
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
	// 💻 3. 後台管理：管理員審核 KYC 實名認證
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
	
	// 🔍 後台輔助查詢：取得單一會員詳細資料 (審核細頁用)
	public MemberVO getOneMember(Integer memberId) {
		return repository.findById(memberId).orElse(null);
	}
	@Transactional
	public void registerMember(MemberVO memberVO) {
		// 🎯 核心修復：讓這個接收 MemberVO 的方法真正做事，加上 @Transactional 並呼叫 save
		
			memberVO.setReportPoints(0);
			memberVO.setServiceRateSum(BigDecimal.ZERO);
			memberVO.setServiceRateCount(0);
			memberVO.setServicerRateSum(BigDecimal.ZERO);
			memberVO.setServicerRateCount(0);
			memberVO.setActRateSum(BigDecimal.ZERO);
			memberVO.setActRateCount(0);
			memberVO.setHoldactRateSum(BigDecimal.ZERO);
			memberVO.setHoldactRateCount(0);
			
			// 2. 真正呼叫 repository 送進資料庫！
			repository.save(memberVO);
		
		
	}
}
