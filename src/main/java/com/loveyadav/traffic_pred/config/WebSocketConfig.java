package com.loveyadav.traffic_pred.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Enable a simple in-memory message broker with destination prefix /topic
        registry.enableSimpleBroker("/topic", "/queue");
        // Prefix for messages bound to @MessageMapping methods
        registry.setApplicationDestinationPrefixes("/app");
        // Prefix for user-specific messages
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry
                .addEndpoint("/ws")
                .setAllowedOriginPatterns("*")   // tighten to your domain in prod
                .withSockJS()
                .setHeartbeatTime(25_000)
                .setDisconnectDelay(5_000);
    }

    @Override
    public void configureWebSocketTransport(WebSocketTransportRegistration registration) {
        registration
                .setMessageSizeLimit(128 * 1024)   // 128 KB
                .setSendBufferSizeLimit(512 * 1024) // 512 KB
                .setSendTimeLimit(20_000);           // 20 s
    }
}