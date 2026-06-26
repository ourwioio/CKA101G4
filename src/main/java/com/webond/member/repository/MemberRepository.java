package com.webond.member.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.webond.member.model.MemberVO;

public interface MemberRepository  extends JpaRepository<MemberVO, Integer> {

}
