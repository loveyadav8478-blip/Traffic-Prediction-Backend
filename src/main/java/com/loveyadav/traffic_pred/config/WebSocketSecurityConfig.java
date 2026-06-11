package com.loveyadav.traffic_pred.config;

import com.loveyadav.traffic_pred.service.CustomUserDetailsService;
import com.loveyadav.traffic_pred.utils.JwtUtil;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class WebSocketSecurityConfig implements WebSocketMessageBrokerConfigurer {

    // Uses your actual class names — no more placeholder imports
    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor =
                        MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

                if (accessor == null || !StompCommand.CONNECT.equals(accessor.getCommand())) {
                    return message;   // not a CONNECT frame — pass through unchanged
                }

                String authHeader = accessor.getFirstNativeHeader("Authorization");
                if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                    // No token supplied — connection is allowed but unauthenticated.
                    // Alerts on /topic/alerts are broadcast to everyone.
                    log.debug("WebSocket CONNECT without Authorization header — anonymous session");
                    return message;
                }

                String token = authHeader.substring(7).trim();
                try {
                    // validateToken() and getUsernameFromToken() are both defined
                    // on your real JwtUtil — no method-name mismatch
                    if (jwtUtil.validateToken(token)) {
                        String email = jwtUtil.getUsernameFromToken(token);
                        UserDetails userDetails = userDetailsService.loadUserByUsername(email);

                        UsernamePasswordAuthenticationToken auth =
                                new UsernamePasswordAuthenticationToken(
                                        userDetails, null, userDetails.getAuthorities());

                        // Attach principal to the STOMP session
                        accessor.setUser(auth);
                        log.debug("WebSocket authenticated: {}", email);
                    }
                } catch (ExpiredJwtException e) {
                    // Token is structurally valid but expired — tell the client clearly
                    log.warn("WebSocket CONNECT rejected — token expired for subject: {}",
                            e.getClaims().getSubject());
                    // Returning message still allows connection as anonymous;
                    // throw new MessageDeliveryException(...) here if you want to hard-reject
                } catch (Exception e) {
                    log.warn("WebSocket CONNECT — token validation failed: {}", e.getMessage());
                }

                return message;
            }
        });
    }
}