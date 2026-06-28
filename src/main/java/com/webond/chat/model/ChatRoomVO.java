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
@Table(name = "chat_room")
public class ChatRoomVO {

	@Id
	@Column(name = "CHAT_ROOM_ID", updatable = false)
	private Integer crmId;	
	
	@ManyToOne
	@JoinColumn(name = "MEMBER1_ID", referencedColumnName = "MEMBER_ID")
	private MemberVO memId1;		
	
	@ManyToOne
	@JoinColumn(name = "MEMBER2_ID", referencedColumnName = "MEMBER_ID")
	private MemberVO memId2;	
	
	@Column(name = "CREATED_AT", updatable = false)
	private Timestamp created;	
	
	public ChatRoomVO() {
		super();
	}	
	
	public ChatRoomVO(Integer crmId, MemberVO memId1, MemberVO memId2, Timestamp created) {
		super();
		this.crmId = crmId;
		this.memId1 = memId1;
		this.memId2 = memId2;
		this.created = created;
	}
	
	public Integer getCrmId() {
		return crmId;
	}

	public void setCrmId(Integer crmId) {
		this.crmId = crmId;
	}
	public Timestamp getCreated() {
		return created;
	}
	public void setCreated(Timestamp created) {
		this.created = created;
	}


	public MemberVO getMemId1() {
		return memId1;
	}


	public void setMemId1(MemberVO memId1) {
		this.memId1 = memId1;
	}


	public MemberVO getMemId2() {
		return memId2;
	}


	public void setMemId2(MemberVO memId2) {
		this.memId2 = memId2;
	}
	
	
	
	
}
