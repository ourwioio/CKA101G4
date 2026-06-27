package com.webond.member.service;

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
		repository.save(notificationVO);
	}
	
	public void updateNotification(NotificationVO notificationVO) {
		repository.save(notificationVO);
	}
	
	public void deleteNotification(Integer notificationId) {
		if(repository.existsById(notificationId)) {
			repository.deleteById(notificationId);
		}
	}
	
	public NotificationVO getOneNotification(Integer notificationId) {
		Optional<NotificationVO> optional = repository.findById(notificationId);
		return optional.orElse(null);
	}
	
	public List<NotificationVO> getAll(){
		return repository.findAll();
	}
	
	

}
