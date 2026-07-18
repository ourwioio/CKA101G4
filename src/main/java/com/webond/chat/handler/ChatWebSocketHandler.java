package com.webond.chat.handler;

import java.util.ArrayList;
import java.util.HashMap;
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
import tools.jackson.databind.node.ObjectNode;

@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

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

		session.getAttributes().put("MEMBER_ID", memberId);
		sessionsMap.put(memberId, session);

	}

// === 處理收到的訊息 === //	
	@Override
	protected void handleTextMessage(WebSocketSession userSession, TextMessage message) throws Exception {
		String payload = message.getPayload();

		// 解析前端傳入的JSON
		ChatMessageDTO chatMessage = objectMapper.readValue(payload, ChatMessageDTO.class);

		Integer senderId = chatMessage.getSenderId();
		Integer receiverId = chatMessage.getReceiverId();

		// 獲取歷史訊息紀錄(type = history)
		if ("history".equals(chatMessage.getType())) {
		    List<String> historyJsonList = chatSvc.getHistoryMsg(String.valueOf(senderId), String.valueOf(receiverId));

		    List<ChatMessageDTO> historyDtos = new ArrayList<>();
		    for (String json : historyJsonList) {
		        historyDtos.add(objectMapper.readValue(json, ChatMessageDTO.class));
		    }

		    Map<String, Object> historyResponse = new HashMap<>();
		    historyResponse.put("type", "history");
		    historyResponse.put("senderId", senderId);
		    historyResponse.put("receiverId", receiverId);
		    historyResponse.put("historyList", historyDtos); // 🚀 完美把陣列塞進去，前端直接用 msgData.historyList 就能讀取

		    if (userSession.isOpen()) {
		        userSession.sendMessage(new TextMessage(objectMapper.writeValueAsString(historyResponse)));
		    }

		    return;
		}

		// 發送即時私訊(type = "chat")
		if ("chat".equals(chatMessage.getType())) {
			WebSocketSession receiverSession = sessionsMap.get(receiverId);
			boolean isReceiverOnline = (receiverSession != null && receiverSession.isOpen());

			chatMessage.setMsgRead(0);
			String cleanJsonMessage = objectMapper.writeValueAsString(chatMessage);

			if (isReceiverOnline) {
				receiverSession.sendMessage(new TextMessage(cleanJsonMessage));
			}

			if (userSession.isOpen()) {
				userSession.sendMessage(new TextMessage(cleanJsonMessage));
			}

			chatSvc.saveChatMessage(String.valueOf(senderId), String.valueOf(receiverId), cleanJsonMessage, false);
			return;
		}

		if ("read".equals(chatMessage.getType())) {
			chatSvc.readChatMessage(String.valueOf(receiverId), String.valueOf(senderId));

			WebSocketSession originalSenderSession = sessionsMap.get(receiverId);

			if (originalSenderSession != null && originalSenderSession.isOpen()) {
				ObjectNode readNotice = objectMapper.createObjectNode();
				readNotice.put("type", "read");
				readNotice.put("senderId", String.valueOf(senderId)); // 告訴對方是誰點開了已讀

				originalSenderSession.sendMessage(new TextMessage(objectMapper.writeValueAsString(readNotice)));
			}
			return;
		}

	}

	@Override
	public void handleTransportError(WebSocketSession session, Throwable e) throws Exception {
		System.out.println("Error: " + e);
	}

//=== 當連線關閉時 ===//	
	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
		Integer memberIdClose = (Integer) session.getAttributes().get("MEMBER_ID");

		if (memberIdClose != null) {
			sessionsMap.remove(memberIdClose);
		}

	}

// === 從連線URI取出memberId === //
	private Integer getMemberId(WebSocketSession session) {
		String path = session.getUri().getPath();
		String idStr = path.substring(path.lastIndexOf('/') + 1);
		return Integer.valueOf(idStr);
	}

}
