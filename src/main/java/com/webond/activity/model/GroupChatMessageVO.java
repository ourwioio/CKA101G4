package com.webond.activity.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "GROUP_CHAT_MESSAGE")
public class GroupChatMessageVO {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "GROUP_CHAT_MESSAGE_ID")
	private Integer groupChatMessageId;

	@Column(name = "ACTIVITY_ID")
	private Integer activityId;

	@Column(name = "SENDER_MEMBER_ID")
	private Integer senderMemberId;

	@Column(name = "CONTENT")
	private String content;

	@Column(name = "SENT_AT", insertable = false, updatable = false)
	private LocalDateTime sentAt;

	public Integer getGroupChatMessageId() {
		return groupChatMessageId;
	}

	public void setGroupChatMessageId(Integer groupChatMessageId) {
		this.groupChatMessageId = groupChatMessageId;
	}

	public Integer getActivityId() {
		return activityId;
	}

	public void setActivityId(Integer activityId) {
		this.activityId = activityId;
	}

	public Integer getSenderMemberId() {
		return senderMemberId;
	}

	public void setSenderMemberId(Integer senderMemberId) {
		this.senderMemberId = senderMemberId;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public LocalDateTime getSentAt() {
		return sentAt;
	}

	public void setSentAt(LocalDateTime sentAt) {
		this.sentAt = sentAt;
	}
}
