package com.webond.chat.handler;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.webond.chat.model.ChatMessageDTO;
import com.webond.chat.service.ChatService;

import tools.jackson.databind.ObjectMapper;

@Component
public class ChatWebSocketHandler extends TextWebSocketHandler{
	
	private final Map<Integer, WebSocketSession> sessionsMap = new ConcurrentHashMap<>();
	
	private final ObjectMapper objectMapper;
	private final ChatService chatSvc;
	public ChatWebSocketHandler(ObjectMapper objectMapper, ChatService chatSvc) {
		this.objectMapper = objectMapper;
		this.chatSvc = chatSvc;
	}
	
	
// === 當連線成功 === //
	@Override
	public void afterConnectionEstablished(WebSocketSession session) throws Exception {
		Integer memberId = getMemberId(session);
		sessionsMap.put(memberId, session);
		
		System.out.println("會員連線成功，編號: " + memberId + " (Session ID: " + session.getId() + ")");
	}
	
// === 處理收到的訊息 === //	
	@Override
	protected void handleTextMessage(WebSocketSession userSession, TextMessage message) throws Exception {
		String payload = message.getPayload();
		
		//解析前端傳入的JSON
		ChatMessageDTO chatMessage = objectMapper.readValue(payload, ChatMessageDTO.class);
	
		Integer senderId = chatMessage.getSenderId();
		Integer receiverId = chatMessage.getReceiverId();
		
		// 獲取歷史訊息紀錄(type = history)
		if("history".equals(chatMessage.getType())) {
			List<String> historyData = chatSvc.getHistoryMsg(String.valueOf(senderId), String.valueOf(receiverId));
			String historyMsg = objectMapper.writeValueAsString(historyData);
			
			ChatMessageDTO cmHistory = new ChatMessageDTO("history", senderId, receiverId, historyMsg);
			if(userSession.isOpen()) {
				userSession.sendMessage(new TextMessage(objectMapper.writeValueAsString(cmHistory)));
			}
			
			WebSocketSession friendSession = sessionsMap.get(senderId);
			if (friendSession != null && friendSession.isOpen()) {
				ChatMessageDTO readNotification = new ChatMessageDTO("read", receiverId, senderId, "READ");
				friendSession.sendMessage(new TextMessage(objectMapper.writeValueAsString(readNotification)));
			}
			
			return;
		}
		// 發送即時私訊(type = chat)
		if("chat".equals(chatMessage.getType())) {
			
			chatMessage.setMsgRead(0);
			String cleanJson = objectMapper.writeValueAsString(chatMessage);
			
			// 傳發給接收者
			WebSocketSession receiverSession = sessionsMap.get(receiverId);
			if(receiverSession != null && receiverSession.isOpen()) {
				receiverSession.sendMessage(new TextMessage(cleanJson));
			}
			
			// 同步回傳給發送者
			if(userSession.isOpen()) {
				userSession.sendMessage(new TextMessage(cleanJson));
			}
			
			//儲存對話紀錄
			chatSvc.saveChatMessage(String.valueOf(senderId), String.valueOf(receiverId), cleanJson);
		
		}
		
		if("read".equals(chatMessage.getType())) {
			chatSvc.readChatMessage(String.valueOf(senderId), String.valueOf(receiverId));
		
			WebSocketSession originalSenderSession = sessionsMap.get(senderId);
			if(originalSenderSession != null && originalSenderSession.isOpen()) {
				String readJson = objectMapper.writeValueAsString(chatMessage);
				originalSenderSession.sendMessage(new TextMessage(readJson));
			}
		}
		
	}
	
	

	@Override
	public void handleTransportError(WebSocketSession session, Throwable e) throws Exception {
		System.out.println("Error: " + e);
	}
	
	
//=== 當連線關閉時 ===//	
	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
		Integer memberIdClose = null;
		//根據session移除對應的會員編號key
		for (Map.Entry<Integer, WebSocketSession> entry : sessionsMap.entrySet()) {
			if(entry.getValue() != null && entry.getValue().equals(session)) {
				memberIdClose = entry.getKey();
				sessionsMap.remove(memberIdClose);
				break;
			}
		}
	
	}
	
// === 從連線URI取出memberId === //
	private Integer getMemberId(WebSocketSession session) {
		String path = session.getUri().getPath();
		String idStr = path.substring(path.lastIndexOf('/')+1);
		return Integer.valueOf(idStr);
	}
	
	
	
	

}
