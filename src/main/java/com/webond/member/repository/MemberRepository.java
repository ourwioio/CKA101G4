package com.webond.member.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.webond.member.model.MemberVO;

public interface MemberRepository  extends JpaRepository<MemberVO, Integer> {

	Optional<MemberVO> findByEmail(String email);
}
