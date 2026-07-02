package com.webond.member.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.webond.member.model.NotificationVO;
import com.webond.member.repository.NotificationRepository;

@Service
public class NotificationService {
	
	@Autowired
	NotificationRepository repository;
	
	@Autowired
	private SessionFactory sessionFactory;
	
	public void addNotification(NotificationVO notificationVO) {
		//自動帶入時間
		notificationVO.setCreatedAt(LocalDate.now());
		notificationVO.setIsRead((byte) 0);
		repository.save(notificationVO);
	}
	
	public void updateNotification(NotificationVO notificationVO) {
		//更新的時間不能修改
		NotificationVO original = repository.findById(notificationVO.getNotificationId()).orElse(null);
		if(original != null) {
			notificationVO.setCreatedAt(original.getCreatedAt());
		}
		repository.save(notificationVO);
	}
	
	public NotificationVO getOneForUpdate(Integer notificationId) {
	    return repository.findById(notificationId).orElse(null);
	}
	
	public void deleteNotification(Integer notificationId) {
		if(repository.existsById(notificationId)) {
			repository.deleteById(notificationId);
		}
	}
	
	public NotificationVO getOneNotification(Integer notificationId) {
		Optional<NotificationVO> optional = repository.findById(notificationId);
		NotificationVO notificationVO = optional.orElse(null);
		if(notificationVO != null) {
			notificationVO.setIsRead((byte) 1);
			repository.save(notificationVO);
		}
		return notificationVO;
	}

	
	public List<NotificationVO> getByMemberId(Integer memberId){
		return repository.findByMember_MemberId(memberId);
	}
	
	public List<NotificationVO> getAll(){
		return repository.findAll();
	}
	
	
	

}
