package com.webond.chat.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class ChatRedisRepository {
	
	private final StringRedisTemplate redisTemplate;

	public ChatRedisRepository(StringRedisTemplate redisTemplate) {
		this.redisTemplate = redisTemplate;
	}
	
// === 儲存單條訊息到Redis的List ===//
	public void saveMessage(String redisKey, String messageJson) {
		redisTemplate.opsForList().rightPush(redisKey, messageJson);
		redisTemplate.opsForList().trim(redisKey, -100, -1);
		redisTemplate.expire(redisKey, 30, TimeUnit.DAYS);
	}
	
// === 從Redis撈出指定Key所有聊天紀錄 ===//
	public List<String> getMessages(String redisKey){
		return redisTemplate.opsForList().range(redisKey, 0, -1);
	}
	
	
// === 一次性將歷史紀錄回填寫入 Redis 的 List === //
	public void saveAllMessages(String redisKey, List<String> messagesList) {
	    if (messagesList != null && !messagesList.isEmpty()) {
	        redisTemplate.opsForList().rightPushAll(redisKey, messagesList);
	        redisTemplate.opsForList().trim(redisKey, -100, -1);
	        redisTemplate.expire(redisKey, 30, java.util.concurrent.TimeUnit.DAYS);
	    }
	}	
	
	
// === 產生新聊天時，更新兩人的聊過天清單，紀錄最新時間戳記 === //
	public void addChatFriend(Integer userId, Integer friendId) {
		String userKey = "chat:friends:" + userId;
		long now = System.currentTimeMillis();
		redisTemplate.opsForZSet().add(userKey, String.valueOf(friendId), now);
	}
	
	
// === 獲取某個使用者聊過天的人（按時間由新到舊排序） === //
	public List<Integer> getChatFriends(Integer userId) {
	    String key = "chat:friends:" + userId;
	    // reverseRange 代表從分數高的（最新）抓到分數低的（最舊）
	    Set<String> members = redisTemplate.opsForZSet().reverseRange(key, 0, -1);
	    
	    List<Integer> friendIds = new ArrayList<>();
	    if (members != null) {
	        for (String id : members) {
	            friendIds.add(Integer.parseInt(id));
	        }
	    }
	    return friendIds;
	}
	
// 刪除指定聊天室的舊快取
	public void deleteMessages(String redisKey) {
		redisTemplate.delete(redisKey);
	}

}
