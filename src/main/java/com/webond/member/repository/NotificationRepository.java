package com.webond.member.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.webond.member.model.NotificationVO;

public interface NotificationRepository  extends JpaRepository<NotificationVO, Integer> {

}
