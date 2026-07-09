package com.webond.member.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.webond.member.model.MemberVO;

@Repository
public interface MemberRepository extends JpaRepository<MemberVO, Integer> {

	// 1. 依 Email 查詢會員 (登入與註冊檢查用)
	Optional<MemberVO> findByEmail(String email);

	// 2. 依帳號狀態查詢會員列表 (給 getMembersByStatus 效能優化使用)
	List<MemberVO> findByAccountStatus(Byte accountStatus);

	// 3. 關鍵字模糊搜尋：Email、真實姓名、手機 (給 searchMembers 效能優化使用)
	List<MemberVO> findByEmailContainingOrRealNameContainingOrPhoneContaining(String email, String realName, String phone);
}