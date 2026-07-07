package com.webond.activity.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.webond.activity.model.GroupChatMessageVO;

public interface GroupChatMessageRepository extends JpaRepository<GroupChatMessageVO, Integer> {

	List<GroupChatMessageVO> findByActivityIdOrderBySentAtAsc(Integer activityId);
}
