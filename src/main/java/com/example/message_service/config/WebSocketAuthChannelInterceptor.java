package com.example.message_service.config;

import com.example.message_service.components.JwtTokenUtil;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Component
@RequiredArgsConstructor
public class WebSocketAuthChannelInterceptor implements ChannelInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketAuthChannelInterceptor.class);
    private final JwtTokenUtil jwtTokenUtil;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String token = accessor.getFirstNativeHeader("Authorization");

            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
                try {
                    Claims claims = jwtTokenUtil.extractClaims(token);
                    String username = claims.getSubject();

                    // ✅ Truyền authorities KHÔNG null (ví dụ gán quyền ROLE_USER)
                    var authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
                    var auth = new UsernamePasswordAuthenticationToken(username, null, authorities);
                    accessor.setUser(auth);

                    logger.info("✅ Authenticated WebSocket user: {}", username);

                } catch (Exception e) {
                    logger.warn("❌ JWT WebSocket error: {}", e.getMessage());
                    throw new IllegalArgumentException("Invalid WebSocket JWT token");
                }
            } else {
                throw new IllegalArgumentException("Missing Authorization header");
            }
        }

        return message;
    }

    // Cấu hình CORS cho REST và SockJS WebSocket
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins("http://localhost:63342")
                        .allowedMethods("*")
                        .allowedHeaders("*")
                        .allowCredentials(true);
            }
        };
    }
}
