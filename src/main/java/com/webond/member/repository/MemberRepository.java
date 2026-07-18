package com.webond.member.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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
	
    @Query("select m.nickname from MemberVO m where m.memberId = :memberId")
    String findNicknameById(@Param("memberId") Integer memberId);
    
    @Modifying
    @Query("UPDATE MemberVO m SET " +
           "m.servicerRateSum = COALESCE(m.servicerRateSum, 0) + :rate, " +
           "m.servicerRateCount = COALESCE(m.servicerRateCount, 0) + 1 " +
           "WHERE m.memberId = :memberId")
    int addServicerRating(@Param("memberId") Integer memberId, @Param("rate") java.math.BigDecimal rate);

    @Modifying
    @Query("UPDATE MemberVO m SET " +
           "m.serviceRateSum = COALESCE(m.serviceRateSum, 0) + :rate, " +
           "m.serviceRateCount = COALESCE(m.serviceRateCount, 0) + 1 " +
           "WHERE m.memberId = :memberId")
    int addServiceRating(@Param("memberId") Integer memberId, @Param("rate") java.math.BigDecimal rate);
    
    @Modifying
    @Query("UPDATE MemberVO m SET " +
           "m.holdactRateSum = COALESCE(m.holdactRateSum, 0) + :rate, " +
           "m.holdactRateCount = COALESCE(m.holdactRateCount, 0) + 1 " +
           "WHERE m.memberId = :memberId")
    int addHoldactRating(@Param("memberId") Integer memberId, @Param("rate") java.math.BigDecimal rate);
}