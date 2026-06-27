package com.webond.member.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.webond.member.model.MemberReportVO;


@Repository
public interface MemberReportRepository extends JpaRepository<MemberReportVO, Integer> {
    
    // 這裡目前不需要寫任何方法，
    // 繼承 JpaRepository 後，你就自動擁有 save(), findAll(), findById(), deleteById() 等功能了！
    
}




