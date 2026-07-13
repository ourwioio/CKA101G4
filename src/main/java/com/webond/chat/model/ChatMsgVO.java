package com.webond.chat.model;

import java.sql.Timestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "chat_message")
public class ChatMsgVO {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "MESSAGE_ID", updatable = false)
	private Integer msgId;

	@Column(name = "SENDER_MEMBER_ID")
	private Integer senderId;
	
	@Column(name = "RECEIVER_MEMBER_ID")
	private Integer receiverId;

	@Column(name = "CONTENT")
	private String content;

	@Column(name = "SENT_AT", updatable = false)
	private Timestamp sentAt;

	@Column(name = "IS_READ")
	private Integer isRead;



	public ChatMsgVO() {
		super();
	}



	public ChatMsgVO(Integer msgId, Integer senderId, Integer receiverId, String content, Timestamp sentAt,
			Integer isRead) {
		super();
		this.msgId = msgId;
		this.senderId = senderId;
		this.receiverId = receiverId;
		this.content = content;
		this.sentAt = sentAt;
		this.isRead = isRead;
	}



	public Integer getMsgId() {
		return msgId;
	}



	public void setMsgId(Integer msgId) {
		this.msgId = msgId;
	}



	public Integer getSenderId() {
		return senderId;
	}



	public void setSenderId(Integer senderId) {
		this.senderId = senderId;
	}



	public Integer getReceiverId() {
		return receiverId;
	}



	public void setReceiverId(Integer receiverId) {
		this.receiverId = receiverId;
	}



	public String getContent() {
		return content;
	}



	public void setContent(String content) {
		this.content = content;
	}



	public Timestamp getSentAt() {
		return sentAt;
	}



	public void setSentAt(Timestamp sentAt) {
		this.sentAt = sentAt;
	}



	public Integer getIsRead() {
		return isRead;
	}



	public void setIsRead(Integer isRead) {
		this.isRead = isRead;
	}

	

}
