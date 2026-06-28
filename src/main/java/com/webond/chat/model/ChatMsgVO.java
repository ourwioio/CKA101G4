package com.webond.chat.model;

import java.sql.Timestamp;

import com.webond.member.model.MemberVO;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "chat_message")
public class ChatMsgVO {

	@Id
	@Column(name = "MESSAGE_ID", updatable = false)
	private Integer msgId;

	@ManyToOne
	@JoinColumn(name = "SENDER_MEMBER_ID", referencedColumnName = "MEMBER_ID")
	private MemberVO sendMemId;

	@Column(name = "CONTENT")
	private String content;

	@Column(name = "SENT_AT", updatable = false)
	private Timestamp sentAt;

	@Column(name = "IS_READ")
	private Integer isRead;

	@ManyToOne
	@JoinColumn(name = "CHAT_ROOM_ID", referencedColumnName = "CHAT_ROOM_ID")
	private ChatRoomVO chatRoomVO;

	public ChatMsgVO() {
		super();
	}

	public ChatMsgVO(Integer msgId, MemberVO sendMemId, String content, Timestamp sentAt, Integer isRead,
			ChatRoomVO chatRoomVO) {
		super();
		this.msgId = msgId;
		this.sendMemId = sendMemId;
		this.content = content;
		this.sentAt = sentAt;
		this.isRead = isRead;
		this.chatRoomVO = chatRoomVO;
	}

	public Integer getMsgId() {
		return msgId;
	}

	public void setMsgId(Integer msgId) {
		this.msgId = msgId;
	}

	public MemberVO getSendMemId() {
		return sendMemId;
	}

	public void setSendMemId(MemberVO sendMemId) {
		this.sendMemId = sendMemId;
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

	public void setChatRoomVO(ChatRoomVO chatRoomVO) {
		this.chatRoomVO = chatRoomVO;
	}

	public ChatRoomVO getChatRoomVO() {
		return chatRoomVO;
	}

}
