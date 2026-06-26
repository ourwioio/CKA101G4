package com.webond.notification.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.webond.notification.model.NotificationVO;

public interface NotificationRepository  extends JpaRepository<NotificationVO, Integer> {

}
