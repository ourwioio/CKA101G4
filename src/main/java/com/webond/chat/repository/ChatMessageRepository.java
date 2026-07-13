package com.webond.chat.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.webond.chat.model.ChatMsgVO;

@Repository
public interface ChatMessageRepository extends JpaRepository <ChatMsgVO, Integer> {
	
	@Query("SELECT c FROM ChatMsgVO c WHERE " +
		   "(c.senderId = :uid1 AND c.receiverId = :uid2) OR " +
		   "(c.senderId = :uid2 AND c.receiverId = :uid1)" +
		   " ORDER BY c.sentAt ASC")
	List<ChatMsgVO> findHistory(@Param("uid1") Integer uid1, @Param("uid2") Integer uid2);
	
	
    @Query("SELECT DISTINCT CASE WHEN c.senderId = :userId THEN c.receiverId ELSE c.senderId END " +
            "FROM ChatMsgVO c WHERE c.senderId = :userId OR c.receiverId = :userId")
     List<Integer> findActiveChatPartners(@Param("userId") Integer userId);
    
    @Modifying
    @Transactional
    @Query("UPDATE ChatMsgVO c SET c.isRead = 1 WHERE c.senderId = :senderId AND c.receiverId = :receiverId AND c.isRead = 0")
    int markMessagesAsRead(@Param("senderId") Integer senderId, @Param("receiverId") Integer receiverId);
}
