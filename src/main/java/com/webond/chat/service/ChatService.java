package com.webond.chat.service;

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
	        vo.setSentAt(new java.sql.Timestamp(System.currentTimeMillis())); 
	        
	        int finalReadStatus = isReceiverInWindow ? 1 : 0;
	        vo.setIsRead(finalReadStatus); 
	        
	        chatMsgRepo.save(vo); 
	        
	        String key = getRedisKey(senderId, receiverId);
	        
	        dto.setMsgRead(finalReadStatus);
	        String updatedPayloadJson = objectMapper.writeValueAsString(dto);
	        
	        chatRedisRepo.saveMessage(key, updatedPayloadJson);
	        
	        chatRedisRepo.addChatFriend(s, r);
	        chatRedisRepo.addChatFriend(r, s);
	        
	    } catch (Exception e) {
	        System.err.println("❌ [Chat Service Error] 處理即時訊息失敗，強制觸發事務回滾！");
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
        
        for (ChatMsgVO vo : dbData) {
            ChatMessageDTO cm = new ChatMessageDTO("chat", vo.getSenderId(), vo.getReceiverId(), vo.getContent(), vo.getIsRead());
            restoredList.add(objectMapper.writeValueAsString(cm));
        }
        
        if (!restoredList.isEmpty()) {
            chatRedisRepo.saveAllMessages(key, restoredList);
        }
        
        return restoredList;
    }
	
// 獲取已聊天聯絡人
	public List<MemberVO> getChatList(Integer currentUserId, Integer toId) {
        List<Integer> fromRedis = chatRedisRepo.getChatFriends(currentUserId);
        
        List<Integer> activeFriendIds = (fromRedis != null) ? new ArrayList<>(fromRedis) : new ArrayList<>();
        
        if (activeFriendIds.isEmpty()) {
            System.out.println("Redis 聊天清單為空，從 MySQL 撈取歷史聊天對象...");
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
                if (mem != null) {
                    chatList.add(mem);
                }
            }
        }
        return chatList;
    }
	
// 處理已讀
	@Transactional(rollbackFor = Exception.class)
    public void readChatMessage(String senderId, String receiverId) {
        Integer s = Integer.parseInt(senderId); // 對方
        Integer r = Integer.parseInt(receiverId); // 自己
        
        int updatedRows = chatMsgRepo.markMessagesAsRead(s, r); 
        
        if (updatedRows > 0) {
            String key = getRedisKey(senderId, receiverId);
            
            chatRedisRepo.deleteMessages(key); 
            try {
                this.getHistoryMsg(senderId, receiverId); // 預熱快取，確保資料強一致性
            } catch (Exception e) {
                System.err.println("⚠️ 已讀後自動預熱快取失敗: " + e.getMessage());
            }
            
        }
    }
	
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
	
	public long getUnreadCount(Integer currentUserId, Integer partnerId) {
	    try {
	        return chatMsgRepo.countUnreadMessages(currentUserId, partnerId);
	    } catch (Exception e) {
	        System.err.println("❌ 計算未讀數失敗: " + e.getMessage());
	        return 0;
	    }
	}
	

}
