package com.webond.member.service;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.webond.member.model.MemberVO;
import com.webond.member.model.NotificationVO;
import com.webond.member.repository.MemberRepository;
import com.webond.venue.model.VenueOrderVO;

import jakarta.transaction.Transactional;

@Service
public class MemberService {
	
	@Autowired
	MemberRepository repository;
	
	@Autowired
	private SessionFactory sessionFactory;
	
	@Autowired
	private PasswordEncoder passwordEncoder;
	
	public void addMember(MemberVO memberVO) {
		repository.save(memberVO);
	}
	

	public void updateMember(MemberVO memberVO) {
		repository.save(memberVO);
	}
	
	public void deleteMember(Integer memberId) {
		if(repository.existsById(memberId)) { //先利用existsById判斷有沒有這項pk
			repository.deleteById(memberId);
		}
	}
	
	public MemberVO getOneMember(Integer memberId) {
		Optional<MemberVO> optional = repository.findById(memberId);
		return optional.orElse(null); //查詢值存在回傳值，否則回傳.orElse()內的值
	}
	
	public List<MemberVO> getALL(){
		return repository.findAll();
	}
	
	public Set<NotificationVO> getNotificationsByMember(Integer memberId){
		return getOneMember(memberId).getNotifications();
	}
	
	public Set<VenueOrderVO> getVenueOrderByMember(Integer memberId){
		return getOneMember(memberId).getVenueOrders();
	}
	
	public void updateMemberStatus(Integer memberId, byte accountStatus) {
	    MemberVO member = repository.findById(memberId).orElse(null);
	    if (member != null) {
	        member.setAccountStatus(accountStatus);
	        repository.save(member);
	    }
	}
	
	@Transactional
	public void changePassword(Integer memberId, String oldPassword, String newPassword) {

	    MemberVO member = repository.findById(memberId).orElse(null);
	    
	    if(member == null) {
	    	throw new IllegalArgumentException("查無此會員");
	    }
		
		if(!passwordEncoder.matches(oldPassword, member.getPasswordHash())) {
			throw new IllegalArgumentException("原密碼輸入錯誤");
		}
		
		if(passwordEncoder.matches(newPassword, oldPassword)) {
			throw new IllegalArgumentException("新密碼不可與原密碼相同");
		}
		
		member.setPasswordHash(passwordEncoder.encode(newPassword));
		repository.save(member);
	}
	
	
	
	
}
