package com.webond.chat.model;


public class ChatMessageDTO {
	private String type;
	private Integer senderId;
	private Integer receiverId;
	private String message;
	private Integer msgRead;
	private String sentAt;
	
	public ChatMessageDTO() {
		super();
	}

	public ChatMessageDTO(String type, Integer senderId, Integer receiverId, String message) {
		this.type = type;
		this.senderId = senderId;
		this.receiverId = receiverId;
		this.message = message;
	}

	

	public ChatMessageDTO(String type, Integer senderId, Integer receiverId, String message, Integer msgRead) {
		super();
		this.type = type;
		this.senderId = senderId;
		this.receiverId = receiverId;
		this.message = message;
		this.msgRead = msgRead;
	}
	
	

	public ChatMessageDTO(String type, Integer senderId, Integer receiverId, String message, Integer msgRead,
			String sentAt) {
		super();
		this.type = type;
		this.senderId = senderId;
		this.receiverId = receiverId;
		this.message = message;
		this.msgRead = msgRead;
		this.sentAt = sentAt;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
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

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Integer getMsgRead() {
		return msgRead;
	}

	public void setMsgRead(Integer msgRead) {
		this.msgRead = msgRead;
	}

	public String getSentAt() {
		return sentAt;
	}

	public void setSentAt(String sentAt) {
		this.sentAt = sentAt;
	}

	
	

	
	
	
}
