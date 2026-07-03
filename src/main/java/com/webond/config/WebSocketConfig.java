package com.webond.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    // 前端 WebSocket 連線入口
    // 前端會連 /ws
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {

        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }

    // 設定 WebSocket 訊息頻道
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {

        // 後端推給前端用，例如 /topic/services/1/slots
        registry.enableSimpleBroker("/topic");

        // 前端送訊息給後端用，例如 /app/xxx
        registry.setApplicationDestinationPrefixes("/app");
    }
}