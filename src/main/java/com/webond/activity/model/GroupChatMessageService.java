package com.webond.activity.model;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.webond.activity.repository.GroupChatMessageRepository;

@Service
@Transactional
public class GroupChatMessageService {

	@Autowired
	private GroupChatMessageRepository repository;

	public List<GroupChatMessageVO> getMessagesByActivityId(Integer activityId) {
		return repository.findByActivityIdOrderBySentAtAsc(activityId);
	}

	public GroupChatMessageVO sendMessage(Integer activityId, Integer senderMemberId, String content) {
		if (content == null || content.trim().isEmpty()) {
			return null;
		}

		GroupChatMessageVO messageVO = new GroupChatMessageVO();
		messageVO.setActivityId(activityId);
		messageVO.setSenderMemberId(senderMemberId);
		messageVO.setContent(content.trim());
		return repository.save(messageVO);
	}
}
