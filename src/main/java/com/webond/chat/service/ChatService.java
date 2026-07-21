package com.webond.chat.service;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.webond.chat.model.ChatMessageDTO;
import com.webond.chat.model.ChatMsgVO;
import com.webond.chat.repository.ChatMessageRepository;
import com.webond.chat.repository.ChatRedisRepository;
import com.webond.member.model.MemberVO;
import com.webond.member.service.MemberService;

import tools.jackson.databind.ObjectMapper;

@Service
public class ChatService {
	
	private final ChatRedisRepository chatRedisRepo;
	private final ChatMessageRepository chatMsgRepo;
	private final ObjectMapper objectMapper;
	private final MemberService memberSvc;
	
	public ChatService(ChatRedisRepository chatRedisRepo, ChatMessageRepository chatMsgRepo,
			ObjectMapper objectMapper, MemberService memberSvc) {
		this.chatRedisRepo = chatRedisRepo;
		this.chatMsgRepo = chatMsgRepo;
		this.objectMapper = objectMapper;
		this.memberSvc = memberSvc;
	}
	
	// 確保同一聊天室用同一個Key
	private String getRedisKey(String senderId, String receiverId) {
		Integer s = Integer.parseInt(senderId);
		Integer r = Integer.parseInt(receiverId);
		return s < r ? "chat:" + s + ":" + r : "chat:" + r + ":" + s ;
	}
	
	// 即時私訊傳入
	@Transactional(rollbackFor = Exception.class) 
	public void saveChatMessage(String senderId, String receiverId, String payloadJson, boolean isReceiverInWindow) {
		Integer s = Integer.parseInt(senderId);
		Integer r = Integer.parseInt(receiverId);
		
		try {
			ChatMessageDTO dto = objectMapper.readValue(payloadJson, ChatMessageDTO.class);
			
			ChatMsgVO vo = new ChatMsgVO();
			vo.setSenderId(dto.getSenderId() != null ? dto.getSenderId() : s);
			vo.setReceiverId(dto.getReceiverId() != null ? dto.getReceiverId() : r);
			vo.setContent(dto.getMessage() != null ? dto.getMessage() : payloadJson);
			vo.setSentAt(new Timestamp(System.currentTimeMillis())); 
			
			long nowMillis = System.currentTimeMillis();
	        vo.setSentAt(new Timestamp(nowMillis)); 
			
			int finalReadStatus = isReceiverInWindow ? 1 : 0;
			vo.setIsRead(finalReadStatus); 
			
			chatMsgRepo.save(vo); 
			
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	        dto.setSentAt(sdf.format(new java.util.Date(nowMillis)));
	        dto.setMsgRead(finalReadStatus);
			
			String key = getRedisKey(senderId, receiverId);
			dto.setMsgRead(finalReadStatus);
			String updatedPayloadJson = objectMapper.writeValueAsString(dto);
			
			chatRedisRepo.saveMessage(key, updatedPayloadJson);
			chatRedisRepo.addChatFriend(s, r);
			chatRedisRepo.addChatFriend(r, s);
			
		} catch (Exception e) {
			throw new RuntimeException("聊天訊息存儲失敗，全案回滾", e);
		}
	}

	// 拿歷史紀錄
	public List<String> getHistoryMsg(String senderId, String receiverId) throws Exception {
		String key = getRedisKey(senderId, receiverId);
		
		List<String> redisData = chatRedisRepo.getMessages(key);
		if (redisData != null && !redisData.isEmpty()) {
			return redisData; 
		}
		
		List<ChatMsgVO> dbData = chatMsgRepo.findHistory(Integer.parseInt(senderId), Integer.parseInt(receiverId));
		List<String> restoredList = new ArrayList<>();
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		
		for (ChatMsgVO vo : dbData) {
			String msgType = "chat";
			if (vo.getContent() != null && vo.getContent().startsWith("data:image/")) {
				msgType = "image";
			}
			
			tools.jackson.databind.node.ObjectNode msgNode = objectMapper.createObjectNode();
			msgNode.put("type", msgType);
			msgNode.put("senderId", vo.getSenderId());
			msgNode.put("receiverId", vo.getReceiverId());
			msgNode.put("message", vo.getContent());
			msgNode.put("msgRead", vo.getIsRead());
			
			if (vo.getSentAt() != null) {
	            msgNode.put("sentAt", sdf.format(vo.getSentAt())); 
	        } else {
	            msgNode.put("sentAt", sdf.format(new java.util.Date()));
	        }
			
			restoredList.add(objectMapper.writeValueAsString(msgNode));
		}
		
		if (!restoredList.isEmpty()) {
			chatRedisRepo.saveAllMessages(key, restoredList);
		}
		return restoredList;
	}
	
	// 處理已讀
	@Transactional(rollbackFor = Exception.class)
	public void readChatMessage(String senderId, String receiverId) {
		Integer s = Integer.parseInt(senderId); // 點開視窗看訊息的人（自己）
		Integer r = Integer.parseInt(receiverId); // 原本發出未讀訊息的人（對方）
		
		// 精準執行已讀：將 [傳送方=r] 且 [接收方=s] 的訊息改成已讀(1)
		int updatedRows = chatMsgRepo.markMessagesAsRead(r, s); 
		
		if (updatedRows > 0) {
			String key = getRedisKey(senderId, receiverId);
			chatRedisRepo.deleteMessages(key); 
			try {
				this.getHistoryMsg(senderId, receiverId); // 快取預熱，維護資料一致性
			} catch (Exception e) {
				System.err.println("⚠️ 已讀後自動預熱快取失敗: " + e.getMessage());
			}
		}
	}

	// 獲取已聊天聯絡人
	public List<MemberVO> getChatList(Integer currentUserId, Integer toId) {
		List<Integer> fromRedis = chatRedisRepo.getChatFriends(currentUserId);
		List<Integer> activeFriendIds = (fromRedis != null) ? new ArrayList<>(fromRedis) : new ArrayList<>();
		
		if (activeFriendIds.isEmpty()) {
			List<Integer> dbFriendIds = chatMsgRepo.findActiveChatPartners(currentUserId);
			if (dbFriendIds != null && !dbFriendIds.isEmpty()) {
				for (Integer friendId : dbFriendIds) {
					activeFriendIds.add(friendId);
					chatRedisRepo.addChatFriend(currentUserId, friendId);
				}
			}
		}
		
		if (toId != null && !activeFriendIds.contains(toId)) {
			activeFriendIds.add(toId);
		}
		
		List<MemberVO> chatList = new ArrayList<>();
		if (!activeFriendIds.isEmpty()) {
			for (Integer id : activeFriendIds) {
				MemberVO mem = memberSvc.getOneMember(id);
				if (mem != null) chatList.add(mem);
			}
		}
		return chatList;
	}
	
	// 獲取動態聯絡人清單摘要與未讀數
	public List<Map<String, Object>> getDynamicChatContacts(Integer currentUserId) {
		List<Map<String, Object>> resultList = new ArrayList<>();
		java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("HH:mm");
		java.time.format.DateTimeFormatter dtf = java.time.format.DateTimeFormatter.ofPattern("HH:mm");
		
		List<Map<String, Object>> dbMetas = chatMsgRepo.findActiveContactsWithMeta(currentUserId);
		if (dbMetas == null || dbMetas.isEmpty()) {
			return resultList;
		}
		
		for (Map<String, Object> meta : dbMetas) {
			Integer senderId = (Integer) meta.get("senderId");
			Integer receiverId = (Integer) meta.get("receiverId");
			Integer partnerId = currentUserId.equals(senderId) ? receiverId : senderId;
			
			MemberVO friend = memberSvc.getOneMember(partnerId);
			if (friend == null) continue;
			
			Map<String, Object> contact = new HashMap<>();
			contact.put("userId", friend.getMemberId());   
			contact.put("username", friend.getRealName());  
			
			String lastMsgText = (String) meta.get("message");
			if (lastMsgText != null && lastMsgText.length() > 10) {
				lastMsgText = lastMsgText.substring(0, 10) + "...";
			}
			contact.put("lastMsg", lastMsgText != null ? lastMsgText : "暫無訊息");
			
			String lastTimeStr = "";
			Object sentAtObj = meta.get("sentAt");
			if (sentAtObj instanceof java.util.Date) {
				lastTimeStr = sdf.format((java.util.Date) sentAtObj);
			} else if (sentAtObj instanceof java.time.LocalDateTime) {
				lastTimeStr = dtf.format((java.time.LocalDateTime) sentAtObj);
			}
			contact.put("lastTime", lastTimeStr);
			
			Number unreadCount = (Number) meta.get("unreadCount");
			contact.put("unreadCount", unreadCount != null ? unreadCount.longValue() : 0L);
			
			resultList.add(contact);
		}
		return resultList;
	}
	
	// 計算未讀數
	public long getUnreadCount(Integer currentUserId, Integer partnerId) {
		try {
			return chatMsgRepo.countUnreadMessages(currentUserId, partnerId);
		} catch (Exception e) {
			return 0;
		}
	}
	
	
}
