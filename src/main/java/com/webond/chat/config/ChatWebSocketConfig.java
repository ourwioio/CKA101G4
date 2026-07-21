package com.webond.chat.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean;

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
	
	@Bean
    public ServletServerContainerFactoryBean createWebSocketContainer() {
        ServletServerContainerFactoryBean container = new ServletServerContainerFactoryBean();
        
        // 設定文字訊息最大快取大小 (50MB)
        container.setMaxTextMessageBufferSize(50 * 1024 * 1024);
        
        // 設定二進位訊息最大快取大小 (50MB)
        container.setMaxBinaryMessageBufferSize(50 * 1024 * 1024);
        
        return container;
    }
	

}
