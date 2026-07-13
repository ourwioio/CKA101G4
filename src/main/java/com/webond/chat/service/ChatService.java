package com.webond.chat.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

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
			ObjectMapper objectMapper,MemberService memberSvc) {
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
	public void saveChatMessage(String senderId, String receiverId, String payloadJson) {
		// 第一時間寫入Redis
		String key = getRedisKey(senderId, receiverId);
		chatRedisRepo.saveMessage(key, payloadJson);
		
		//同步更新好友清單
		Integer s = Integer.parseInt(senderId);
	    Integer r = Integer.parseInt(receiverId);
	    chatRedisRepo.addChatFriend(s, r);
	    
	    //訊息存入資料庫
	    try {
	    	
	    	ChatMessageDTO dto = objectMapper.readValue(payloadJson, ChatMessageDTO.class);
            
            ChatMsgVO vo = new ChatMsgVO();
            vo.setSenderId(dto.getSenderId());
            vo.setReceiverId(dto.getReceiverId());
            vo.setContent(dto.getMessage());
            vo.setSentAt(new java.sql.Timestamp(System.currentTimeMillis())); // 記錄當前系統時間
            vo.setIsRead(0); // 預設 0 代表未讀
            
            chatMsgRepo.save(vo);
	    	
	    }catch(Exception e) {
            System.err.println("❌ 訊息寫入 MySQL 資料庫失敗: " + e.getMessage());
        }
	    
	}

// 拿歷史紀錄
	public List<String> getHistoryMsg(String senderId, String receiverId) throws Exception{
		String key = getRedisKey(senderId, receiverId);
		
		int updateRows = chatMsgRepo.markMessagesAsRead(Integer.parseInt(senderId), Integer.parseInt(receiverId));
		
		if(updateRows >0) {
			chatRedisRepo.deleteMessages(key);
		}
		
		// 先拿Redis
		List<String> redisData = chatRedisRepo.getMessages(key);
		if(redisData != null && !redisData.isEmpty()) {
			return redisData;
		}
		
		// Redis過期去資料庫拿
		List<ChatMsgVO> dbData = chatMsgRepo.findHistory(Integer.parseInt(senderId), Integer.parseInt(receiverId));
		
		List<String> restoredList = new ArrayList<>();
		for (ChatMsgVO vo : dbData) {
			ChatMessageDTO cm = new ChatMessageDTO("chat", vo.getSenderId(), vo.getReceiverId(), vo.getContent(), vo.getIsRead());
			String jsonStr = objectMapper.writeValueAsString(cm);
		
			restoredList.add(jsonStr);
		}
		
		// 回填Redis
		if (!restoredList.isEmpty()) {
			chatRedisRepo.saveAllMessages(key, restoredList);
		}
		
		return restoredList;
	}
	
// 獲取已聊天聯絡人
	public List<MemberVO> getChatList(Integer currentUserId, Integer toId){
	
		List<Integer> activeFriendIds = chatRedisRepo.getChatFriends(currentUserId);
		
		if(activeFriendIds.isEmpty()) {
			System.out.println("Redis 聊天清單為空，從 MySQL 撈取歷史聊天對象...");
			
			List<Integer> dbFriendIds = chatMsgRepo.findActiveChatPartners(currentUserId);
			
			if(dbFriendIds != null && !dbFriendIds.isEmpty()) {
				for(Integer friendId : dbFriendIds) {
					activeFriendIds.add(friendId);
					chatRedisRepo.addChatFriend(currentUserId, friendId);
				}
			}
		}
		
		
		
		if (toId != null && !activeFriendIds.contains(toId)) {
			activeFriendIds.add(toId);
		}
		
		List<MemberVO> chatList = new ArrayList<>();
		if(!activeFriendIds.isEmpty()) {
			for(Integer id : activeFriendIds) {
				MemberVO mem = memberSvc.getOneMember(id);
				if(mem != null) {
					chatList.add(mem);
				}
			}
		}
		return chatList;
	}
	
// 處理已讀
	public void readChatMessage(String senderId, String receiverId) {
		Integer s = Integer.parseInt(senderId); 
        Integer r = Integer.parseInt(receiverId); 
        
        chatMsgRepo.markMessagesAsRead(s, r);
        
	}
	
	
	

}
