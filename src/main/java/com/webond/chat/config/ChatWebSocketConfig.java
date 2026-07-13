package com.webond.chat.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import com.webond.chat.handler.ChatWebSocketHandler;

@Configuration
@EnableWebSocket
public class ChatWebSocketConfig implements WebSocketConfigurer{
	
	@Autowired
	private ChatWebSocketHandler chatHandler;

	@Override
	public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
		registry.addHandler(chatHandler, "/FriendWS/{userId}")
				.setAllowedOrigins("*");
			
	}
	
	

}
