package com.webond.chat.repository;

import java.util.List;
import java.util.Map;

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
    @Query("UPDATE ChatMsgVO c SET c.isRead = 1 WHERE c.senderId = :authorId AND c.receiverId = :readerId AND c.isRead = 0")
    int markMessagesAsRead(@Param("authorId") Integer authorId, @Param("readerId") Integer readerId);

    @Query(value = "SELECT * FROM chat_message WHERE (SENDER_MEMBER_ID = :u1 AND RECEIVER_MEMBER_ID = :u2) OR (SENDER_MEMBER_ID = :u2 AND RECEIVER_MEMBER_ID = :u1) ORDER BY SENT_AT DESC LIMIT 1", nativeQuery = true)
    ChatMsgVO findLastMessage(@Param("u1") Integer u1, @Param("u2") Integer u2);

    @Query("SELECT COUNT(m) FROM ChatMsgVO m WHERE m.senderId = :partnerId AND m.receiverId = :currentUserId AND m.isRead = 0")
    Integer countUnreadMessages(@Param("currentUserId") Integer currentUserId, @Param("partnerId") Integer partnerId);
   
    
    @Query(value = "SELECT " +
            "    m.MESSAGE_ID as msgId, " +
            "    m.SENDER_MEMBER_ID as senderId, " +
            "    m.RECEIVER_MEMBER_ID as receiverId, " +
            "    CAST(m.CONTENT AS CHAR(4000000)) as message, " + 
            "    m.SENT_AT as sentAt, " +
            "    (SELECT COUNT(*) FROM chat_message u " +
            "     WHERE u.SENDER_MEMBER_ID = CASE WHEN m.SENDER_MEMBER_ID = :currentUserId THEN m.RECEIVER_MEMBER_ID ELSE m.SENDER_MEMBER_ID END " +
            "       AND u.RECEIVER_MEMBER_ID = :currentUserId " +
            "       AND u.IS_READ = 0) as unreadCount " +
            "FROM chat_message m " +
            "WHERE m.MESSAGE_ID IN (" +
            "    SELECT MAX(sub.MESSAGE_ID) " +
            "    FROM chat_message sub " +
            "    WHERE sub.SENDER_MEMBER_ID = :currentUserId OR sub.RECEIVER_MEMBER_ID = :currentUserId " +
            "    GROUP BY CASE WHEN sub.SENDER_MEMBER_ID = :currentUserId THEN sub.RECEIVER_MEMBER_ID ELSE sub.SENDER_MEMBER_ID END " +
            ") " +
            "ORDER BY m.SENT_AT DESC", nativeQuery = true)
List<Map<String, Object>> findActiveContactsWithMeta(@Param("currentUserId") Integer currentUserId);
}


