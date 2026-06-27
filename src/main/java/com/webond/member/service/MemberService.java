package com.webond.member.service;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.webond.member.model.MemberVO;
import com.webond.member.model.NotificationVO;
import com.webond.member.repository.MemberRepository;
import com.webond.venue.model.VenueOrderVO;

@Service
public class MemberService {
	
	@Autowired
	MemberRepository repository;
	
	@Autowired
	private SessionFactory sessionFactory;
	
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
	
	
	
}
